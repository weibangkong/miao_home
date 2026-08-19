/**
 * 喵之家 — 流浪猫社区认养小程序
 */

const config = require('./utils/config');

App({
  globalData: {
    tenantId: config.DEFAULT_TENANT_ID,
    communityName: null,
    adopterId: null,
    adopterInfo: null,
  },

  onLaunch() {
    // 读取本地存储的认养人信息
    const adopterId = wx.getStorageSync('adopterId');
    const adopterInfo = wx.getStorageSync('adopterInfo');

    if (adopterId) {
      this.globalData.adopterId = adopterId;
    }
    if (adopterInfo) {
      this.globalData.adopterInfo = adopterInfo;
    }

    // 读取租户
    const tenantId = wx.getStorageSync('tenantId');
    if (tenantId) {
      this.globalData.tenantId = tenantId;
    }

    // 读取小区名称
    const communityName = wx.getStorageSync('communityName');
    if (communityName) {
      this.globalData.communityName = communityName;
    }
  },

  /**
   * 设置当前认养人
   */
  setAdopter(adopter) {
    this.globalData.adopterId = adopter.id;
    this.globalData.adopterInfo = adopter;
    wx.setStorageSync('adopterId', adopter.id);
    wx.setStorageSync('adopterInfo', adopter);
  },

  /**
   * 清除认养人信息
   */
  clearAdopter() {
    this.globalData.adopterId = null;
    this.globalData.adopterInfo = null;
    wx.removeStorageSync('adopterId');
    wx.removeStorageSync('adopterInfo');
  },
});
