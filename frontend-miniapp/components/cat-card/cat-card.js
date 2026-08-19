/**
 * 猫咪卡片组件
 * 用于列表页展示猫咪缩略信息
 */

const api = require('../../utils/api');

Component({
  properties: {
    cat: {
      type: Object,
      value: {},
    },
  },

  data: {
    avatarUrl: '',
  },

  observers: {
    'cat.avatarUrl'(url) {
      this.setData({
        avatarUrl: api.getFileUrl(url),
      });
    },
  },

  methods: {
    onTap() {
      const cat = this.data.cat;
      wx.navigateTo({
        url: `/pages/cat-detail/index?id=${cat.id}`,
      });
    },
  },
});
