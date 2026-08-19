/**
 * 首页 — 猫咪列表
 */

const api = require('../../utils/api');

Page({
  data: {
    cats: [],
    myCats: [],
    otherCats: [],
    buildings: [],
    filteredMyCats: [],
    filteredOtherCats: [],
    loading: true,
    buildingFilter: '',
    searchText: '',
    communityName: '',
    communities: [],
    adopterId: null,
    hasMyCats: false,
    stats: {
      total: 0,
      adopted: 0,
      pending: 0,
      buildings: 0,
    },
  },

  onLoad() {
    this.refreshCommunity();
    this.loadData();
  },

  onShow() {
    this.refreshCommunity();
    this.loadData();
  },

  /** 刷新当前小区名称和认养人ID */
  refreshCommunity() {
    const app = getApp();
    const name = app.globalData.communityName || '选择小区';
    const adopterId = app.globalData.adopterId || null;
    this.setData({ communityName: name, adopterId });
  },

  /** 切换小区 */
  onSwitchCommunity() {
    wx.navigateTo({ url: '/pages/community/index' });
  },

  /** 跳转猫咪详情 */
  onCatTap(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/cat-detail/index?id=${id}` });
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const catRes = await api.getCats(this.data.buildingFilter || undefined);
      if (catRes.code !== 200) return;
      const cats = catRes.data || [];

      const adopted = cats.filter(c => c.isAdopted).length;
      const buildings = [...new Set(cats.map(c => c.building).filter(Boolean))];

      // 处理头像完整 URL
      cats.forEach(c => {
        c.avatarFullUrl = api.getFileUrl(c.avatarUrl);
      });

      // 获取"我的认养"猫咪ID列表（通过认养记录交叉匹配）
      const adopterId = this.data.adopterId;
      let myCatIds = new Set();
      if (adopterId) {
        try {
          const adopterRes = await api.getAllAdopters();
          if (adopterRes.code === 200) {
            (adopterRes.data || []).forEach(a => {
              if (a.id === adopterId && a.catId) {
                myCatIds.add(a.catId);
              }
            });
          }
        } catch {
          // 认养记录加载失败，回退：用户有认养身份时，已认养的猫视为自己的
          cats.forEach(c => {
            if (c.isAdopted) myCatIds.add(c.id);
          });
        }
      }

      // 分离"我的认养"和"其他猫咪"
      const myCats = [];
      const otherCats = [];
      cats.forEach(c => {
        if (myCatIds.has(c.id)) {
          myCats.push(c);
        } else {
          otherCats.push(c);
        }
      });

      this.setData({
        cats, myCats, otherCats, buildings,
        hasMyCats: myCats.length > 0,
        stats: {
          total: cats.length,
          adopted,
          pending: cats.length - adopted,
          buildings: buildings.length,
        },
      });
      this.computeFilteredCats();
    } catch (e) {
      // 错误已在 api 层处理
    } finally {
      this.setData({ loading: false });
    }
  },

  /** 搜索 */
  onSearchInput(e) {
    this.setData({ searchText: e.detail.value });
    this.computeFilteredCats();
  },

  /** 楼栋筛选 */
  onBuildingTap(e) {
    const b = e.currentTarget.dataset.building;
    const newFilter = this.data.buildingFilter === b ? '' : b;
    this.setData({ buildingFilter: newFilter });
    this.loadData();
  },

  /** 根据搜索文本计算过滤后的猫咪列表 */
  computeFilteredCats() {
    const { myCats, otherCats, searchText } = this.data;
    if (!searchText) {
      this.setData({ filteredMyCats: myCats, filteredOtherCats: otherCats });
      return;
    }
    const kw = searchText.toLowerCase();
    const match = c => (c.name || '').toLowerCase().includes(kw);
    this.setData({
      filteredMyCats: myCats.filter(match),
      filteredOtherCats: otherCats.filter(match),
    });
  },
});
