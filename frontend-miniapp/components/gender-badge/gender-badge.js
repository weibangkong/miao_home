/**
 * 性别徽章组件
 * 蓝色♂ / 粉色♀ / 灰色?
 */

Component({
  properties: {
    gender: {
      type: String,
      value: '',
    },
    /** 是否只显示图标 */
    iconOnly: {
      type: Boolean,
      value: false,
    },
  },

  data: {
    classNames: '',
    displayText: '',
  },

  observers: {
    'gender, iconOnly'(gender, iconOnly) {
      this.updateDisplay(gender, iconOnly);
    },
  },

  methods: {
    updateDisplay(gender, iconOnly) {
      let cls = '';
      let text = '';

      if (gender === '公') {
        cls = 'gender-male';
        text = '♂';
      } else if (gender === '母') {
        cls = 'gender-female';
        text = '♀';
      } else {
        cls = 'gender-unknown';
        text = '?';
      }

      if (!iconOnly && gender) {
        text = text + ' ' + gender;
      }

      this.setData({ classNames: cls, displayText: text });
    },
  },

  lifetimes: {
    attached() {
      this.updateDisplay(this.data.gender, this.data.iconOnly);
    },
  },
});
