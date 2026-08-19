/**
 * 小区选择页
 * 首次启动 / 手动切换小区
 */

const api = require('../../utils/api');

Page({
  data: {
    communities: [],
    currentTenantId: null,
    loading: true,
  },

  onLoad() {
    const app = getApp();
    this.setData({ currentTenantId: app.globalData.tenantId });
    this.loadCommunities();
  },

  async loadCommunities() {
    this.setData({ loading: true });
    try {
      const res = await api.getTenants();
      if (res.code === 200) {
        this.setData({ communities: res.data || [] });
      }
    } catch {
      // 加载失败
    } finally {
      this.setData({ loading: false });
    }
  },

  /** 选择小区 */
  onSelect(e) {
    const id = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name;

    const app = getApp();
    app.globalData.tenantId = id;
    app.globalData.communityName = name;
    wx.setStorageSync('tenantId', id);
    wx.setStorageSync('communityName', name);

    wx.showToast({ title: '已切换到 ' + name, icon: 'success', duration: 1500 });

    // 延迟返回
    setTimeout(() => {
      // 如果是从 mine 页跳转来的，返回；否则回到首页
      const pages = getCurrentPages();
      if (pages.length > 1) {
        wx.navigateBack();
      } else {
        wx.switchTab({ url: '/pages/index/index' });
      }
    }, 1500);
  },
});
