/**
 * 认养表单页
 */

const api = require('../../utils/api');

Page({
  data: {
    catId: 0,
    catName: '',
    form: {
      householdNumber: '',
      adopterName: '',
      phone: '',
      building: '',
      unitNumber: '',
    },
    submitting: false,
  },

  onLoad(options) {
    this.setData({
      catId: Number(options.catId) || 0,
      catName: decodeURIComponent(options.catName || ''),
    });
  },

  /** 表单输入 */
  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`form.${field}`]: e.detail.value,
    });
  },

  /** 提交认养 */
  async onSubmit() {
    const { catId, form } = this.data;

    // 户号必填
    if (!form.householdNumber.trim()) {
      wx.showToast({ title: '请输入认养户号', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    try {
      const res = await api.adoptCat(catId, {
        householdNumber: form.householdNumber.trim(),
        adopterName: form.adopterName.trim(),
        phone: form.phone.trim(),
        building: form.building.trim(),
        unitNumber: form.unitNumber.trim(),
      });

      if (res.code === 200) {
        // 保存认养人信息到全局
        const app = getApp();
        app.setAdopter({
          id: res.data.id,
          ...res.data,
        });

        wx.showToast({
          title: '认养成功！',
          icon: 'success',
          duration: 2000,
        });

        // 延迟返回上一页
        setTimeout(() => {
          wx.navigateBack();
        }, 2000);
      }
    } catch {
      // api 层已处理错误提示
    } finally {
      this.setData({ submitting: false });
    }
  },
});
