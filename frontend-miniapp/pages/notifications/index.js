/**
 * 通知中心页
 */

const api = require('../../utils/api');

Page({
  data: {
    notifications: [],
    unreadCount: 0,
    loading: true,
    adopterId: null,
  },

  onLoad() {
    const app = getApp();
    this.setData({ adopterId: app.globalData.adopterId });
    this.loadData();
  },

  onShow() {
    // 可能认证状态已变化
    const app = getApp();
    if (app.globalData.adopterId !== this.data.adopterId) {
      this.setData({ adopterId: app.globalData.adopterId });
    }
    this.loadData();
  },

  async loadData() {
    const { adopterId } = this.data;
    if (!adopterId) {
      this.setData({ loading: false, notifications: [], unreadCount: 0 });
      return;
    }

    try {
      const res = await api.getNotifications(adopterId);
      if (res.code === 200) {
        const notifications = (res.data || []).map(n => ({
          ...n,
          createdAtFormatted: this.formatTime(n.createdAt),
        }));
        const unreadCount = notifications.filter(n => !n.isRead).length;
        this.setData({ notifications, unreadCount });
      }
    } finally {
      this.setData({ loading: false });
    }
  },

  /** 标记已读 */
  async onMarkRead(e) {
    const id = e.currentTarget.dataset.id;
    try {
      await api.markNotificationRead(id);
      // 更新本地数据
      const notifications = this.data.notifications.map(n =>
        n.id === id ? { ...n, isRead: true } : n
      );
      this.setData({
        notifications,
        unreadCount: notifications.filter(n => !n.isRead).length,
      });
      wx.showToast({ title: '已标记已读', icon: 'success' });
    } catch {}
  },

  /** 全部标记已读 */
  async onMarkAllRead() {
    const { adopterId } = this.data;
    if (!adopterId) return;
    try {
      await api.markAllNotificationsRead(adopterId);
      const notifications = this.data.notifications.map(n => ({ ...n, isRead: true }));
    this.setData({ notifications, unreadCount: 0 });
    wx.showToast({ title: '已全部标记已读', icon: 'success' });
    } catch {}
  },

  /** 格式化时间 */
  formatTime(t) {
    if (!t) return '';
    const parts = t.split('T');
    if (parts.length < 2) return t;
    return parts[0] + ' ' + (parts[1]?.split('.')[0] || '');
  },
});
