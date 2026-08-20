const api = require('../../utils/api');

Page({
  data: {
    cat: null,
    mediaList: [],
    healthRecords: [],
    adopters: [],
    locationGroups: [],
    loading: true,
    sickCount: 0,
    healthyCount: 0,
    // likes
    catLiked: false,
    catLikeCount: 0,
    currentUserId: null,
    // comments
    comments: [],
    commentSort: 'created_at_desc',
    commentInput: '',
    canSubmit: false,
    replyTarget: null,
    commentLoading: false,
  },

  onLoad(options) {
    const id = options.id;
    if (id) {
      this.catId = Number(id);
      this.initUser();
    }
  },

  onShow() {
    if (this.catId) this.loadData();
  },

  /** 初始化用户 */
  async initUser() {
    let userId = wx.getStorageSync('miao_user_id');
    if (!userId) {
      try {
        const nickname = '猫友' + Math.random().toString(36).slice(2, 6);
        const res = await api.createUser({ nickname });
        if (res.code === 200 && res.data) {
          userId = res.data.id;
          wx.setStorageSync('miao_user_id', userId);
        }
      } catch {}
    }
    this.setData({ currentUserId: userId });
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const res = await api.getCatDetail(this.catId);
      if (res.code === 200) {
        const cat = res.data;
        const mediaList = cat.mediaList || [];
        const healthRecords = cat.healthRecordList || [];
        const sickCount = healthRecords.filter(r => r.isSick).length;

        if (cat.avatarUrl) cat.avatarFullUrl = api.getFileUrl(cat.avatarUrl);
        mediaList.forEach(m => { m.fullUrl = api.getFileUrl(m.url); });

        const locationList = cat.locationList || [];
        const locMap = {};
        locationList.forEach(loc => {
          const key = loc.tenantId || 0;
          if (!locMap[key]) locMap[key] = { tenantId: loc.tenantId, buildings: new Set() };
          if (loc.building) locMap[key].buildings.add(loc.building);
        });
        const locationGroups = Object.values(locMap).map(g => ({
          tenantId: g.tenantId,
          buildings: [...g.buildings].join('、'),
        }));

        this.setData({
          cat, mediaList, healthRecords, locationGroups,
          sickCount, healthyCount: healthRecords.length - sickCount,
          catLikeCount: cat.likeCount || 0,
        });

        this.loadAdopters();
        this.loadComments();

        // load cat like status
        const uid = this.data.currentUserId;
        if (uid) {
          api.getCatLikeStatus(this.catId, uid).then(r => {
            if (r.code === 200) this.setData({ catLiked: r.data.liked });
          });
        }
      }
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadAdopters() {
    try {
      const res = await api.getAdoptersByCat(this.catId);
      if (res.code === 200) this.setData({ adopters: res.data || [] });
    } catch {}
  },

  async loadComments() {
    this.setData({ commentLoading: true });
    try {
      const m = this.data.commentSort.match(/^(.+)_(asc|desc)$/);
      const sortBy = m ? m[1] : 'created_at';
      const order = m ? m[2] : 'desc';
      const uid = this.data.currentUserId;
      const res = await api.getComments(this.catId, sortBy, order, uid || undefined);
      if (res.code === 200) {
        const comments = (res.data || []).map(c => ({
          ...c,
          avatarChar: c.nickname ? c.nickname.charAt(0) : '',
          replies: (c.replies || []).map(r => ({
            ...r,
            avatarChar: r.nickname ? r.nickname.charAt(0) : '',
          })),
        }));
        this.setData({ comments });
      }
    } finally {
      this.setData({ commentLoading: false });
    }
  },

  /** 点赞猫咪 */
  async onCatLike() {
    const uid = this.data.currentUserId;
    if (!uid) return;
    const res = await api.toggleCatLike(this.catId, uid);
    if (res.code === 200) {
      this.setData({ catLiked: res.data.liked, catLikeCount: res.data.likeCount });
    }
  },

  /** 评论排序 */
  onCommentSort(e) { this.setData({ commentSort: e.currentTarget.dataset.sort }); this.loadComments(); },

  /** 输入评论 */
  onCommentInput(e) {
    const value = e.detail.value;
    this.setData({ commentInput: value, canSubmit: value.trim().length > 0 });
  },

  /** 提交评论 */
  async onSubmitComment() {
    const content = this.data.commentInput.trim();
    const uid = this.data.currentUserId;
    if (!content || !uid) return;
    const isReply = Boolean(this.data.replyTarget);
    const data = { userId: uid, content };
    if (this.data.replyTarget) data.parentId = this.data.replyTarget.id;
    const res = await api.createComment(this.catId, data);
    if (res.code === 200) {
      this.setData({ commentInput: '', canSubmit: false, replyTarget: null });
      this.loadComments();
      wx.showToast({ title: isReply ? '回复成功' : '评论成功', icon: 'success' });
    }
  },

  /** 回复 */
  onReply(e) {
    const { id, nickname } = e.currentTarget.dataset;
    this.setData({ replyTarget: { id, nickname }, commentInput: '', canSubmit: false });
  },
  onCancelReply() { this.setData({ replyTarget: null, commentInput: '', canSubmit: false }); },

  /** 评论点赞 */
  async onCommentLike(e) {
    const commentId = e.currentTarget.dataset.id;
    const uid = this.data.currentUserId;
    if (!uid) return;
    await api.toggleCommentLike(commentId, uid);
    this.loadComments();
  },

  /** 删除评论 */
  async onDeleteComment(e) {
    const commentId = e.currentTarget.dataset.id;
    const uid = this.data.currentUserId;
    if (!uid) return;
    const res = await api.deleteComment(commentId, uid);
    if (res.code === 200) {
      wx.showToast({ title: '已删除', icon: 'success' });
      this.loadComments();
    }
  },

  onAdopt() {
    const cat = this.data.cat;
    if (!cat) return;
    wx.navigateTo({
      url: `/pages/adopt/index?catId=${cat.id}&catName=${encodeURIComponent(cat.name || '')}`,
    });
  },

  onPreviewMedia(e) {
    const url = e.currentTarget.dataset.url;
    if (!url) return;
    const photos = this.data.mediaList.filter(m => m.mediaType === 'PHOTO').map(m => m.fullUrl);
    if (photos.length === 0) photos.push(url);
    wx.previewImage({ current: url, urls: photos });
  },
});
