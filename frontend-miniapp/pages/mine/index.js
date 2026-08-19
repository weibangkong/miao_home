/**
 * 我的页面 — 认养人信息和认养记录
 */

const api = require('../../utils/api');

Page({
  data: {
    adopterId: null,
    adopterInfo: null,
    hasIdentity: false,
    unreadCount: 0,
    communityName: '',
  },

  onLoad() {
    this.refreshState();
  },

  onShow() {
    this.refreshState();
  },

  refreshState() {
    const app = getApp();
    const adopterId = app.globalData.adopterId;
    const adopterInfo = app.globalData.adopterInfo;
    const communityName = app.globalData.communityName || '未选择';

    this.setData({
      adopterId,
      adopterInfo,
      hasIdentity: Boolean(adopterId),
      communityName,
    });

    // 获取未读通知数
    if (adopterId) {
      api.getUnreadCount(adopterId).then(res => {
        if (res.code === 200) {
          this.setData({ unreadCount: res.data || 0 });
        }
      }).catch(() => {});
    }
  },

  /** 绑定认养人（手动输入认养人ID） */
  onBindAdopter() {
    wx.showModal({
      title: '绑定认养人',
      content: '请输入您的认养人ID\n（认养猫咪后自动获得）',
      editable: true,
      placeholderText: '输入认养人ID',
      success: async (modalRes) => {
        if (modalRes.confirm && modalRes.content) {
          const id = Number(modalRes.content.trim());
          if (!id) {
            wx.showToast({ title: 'ID无效', icon: 'none' });
            return;
          }
          // 简单记录认养人ID
          const app = getApp();
          app.setAdopter({ id });
          this.refreshState();
          wx.showToast({ title: '绑定成功', icon: 'success' });
        }
      },
    });
  },

  /** 解除绑定 */
  onClearIdentity() {
    wx.showModal({
      title: '解除绑定',
      content: '确定要清除当前认养人信息吗？',
      success: (res) => {
        if (res.confirm) {
          const app = getApp();
          app.clearAdopter();
          this.setData({
            adopterId: null,
            adopterInfo: null,
            hasIdentity: false,
            unreadCount: 0,
          });
          wx.showToast({ title: '已解除', icon: 'success' });
        }
      },
    });
  },

  /** 跳转通知 */
  goNotifications() {
    wx.switchTab({ url: '/pages/notifications/index' });
  },

  /** 跳转活动范围 */
  goRange() {
    wx.switchTab({ url: '/pages/range/index' });
  },

  /** 切换小区 */
  goCommunity() {
    wx.navigateTo({ url: '/pages/community/index' });
  },
});
