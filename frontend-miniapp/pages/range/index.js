/**
 * 活动范围页 — 展示猫咪在各楼栋的分布
 */

const api = require('../../utils/api');

Page({
  data: {
    cats: [],
    buildings: [],
    selectedBuilding: '',
    buildingCats: [],
    showCats: false,
    loading: true,
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const res = await api.getCats();
      if (res.code === 200) {
        const cats = res.data || [];
        const buildingMap = {};

        cats.forEach(cat => {
          const b = cat.building || '未知';
          if (!buildingMap[b]) {
            buildingMap[b] = { name: b, total: 0, adopted: 0, pending: 0 };
          }
          buildingMap[b].total++;
          if (cat.isAdopted) {
            buildingMap[b].adopted++;
          } else {
            buildingMap[b].pending++;
          }
        });

        // 处理头像完整 URL
        cats.forEach(c => {
          c.avatarFullUrl = api.getFileUrl(c.avatarUrl);
        });

        const buildings = Object.values(buildingMap).sort((a, b) => b.total - a.total);

        this.setData({ cats, buildings });
      }
    } finally {
      this.setData({ loading: false });
    }
  },

  /** 点击楼栋查看猫咪 */
  onBuildingTap(e) {
    const building = e.currentTarget.dataset.building;
    const cats = this.data.cats.filter(c => (c.building || '未知') === building);
    // 确保筛选出来的猫咪也有完整 URL（cat-card 组件内部也会做，但提前做更安全）
    cats.forEach(c => {
      if (!c.avatarFullUrl) {
        c.avatarFullUrl = api.getFileUrl(c.avatarUrl);
      }
    });

    this.setData({
      selectedBuilding: building,
      buildingCats: cats,
      showCats: true,
    });
  },

  /** 关闭弹窗 */
  onCloseCats() {
    this.setData({ showCats: false });
  },

  /** 跳转猫咪详情 */
  onCatTap(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/cat-detail/index?id=${id}`,
    });
  },
});
