const config = require('./config');

function request(path, options = {}) {
  const { method = 'GET', data, params } = options;

  let url = config.BASE_URL + path;
  if (params) {
    const qs = Object.keys(params)
      .filter(k => params[k] !== undefined && params[k] !== null && params[k] !== '')
      .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
      .join('&');
    if (qs) url += '?' + qs;
  }

  const tenantId = wx.getStorageSync('tenantId') || config.DEFAULT_TENANT_ID;
  const userId = wx.getStorageSync('miao_user_id') || '';

  return new Promise((resolve, reject) => {
    wx.request({
      url,
      method,
      header: {
        'Content-Type': 'application/json',
        'X-Tenant-Id': String(tenantId),
      },
      data: data ? JSON.stringify(data) : undefined,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else {
          wx.showToast({ title: res.data?.message || '请求失败', icon: 'none' });
          reject(res.data);
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常，请稍后再试', icon: 'none' });
        reject(err);
      },
    });
  });
}

function uploadFile(path, filePath, formData = {}) {
  const tenantId = wx.getStorageSync('tenantId') || config.DEFAULT_TENANT_ID;

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: config.BASE_URL + path,
      filePath,
      name: 'file',
      header: { 'X-Tenant-Id': String(tenantId) },
      formData,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(JSON.parse(res.data));
        } else {
          wx.showToast({ title: '上传失败', icon: 'none' });
          reject(res);
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(err);
      },
    });
  });
}

// ====== Cats ======
function getCats(building) { return request('/cats/list', { params: { building } }); }
function getCatDetail(id) { return request(`/cats/${id}`); }

// ====== User ======
function createUser(data) { return request('/users', { method: 'POST', data }); }

// ====== Cat Likes ======
function toggleCatLike(catId, userId) { return request(`/cats/${catId}/like`, { method: 'POST', data: { userId } }); }
function getCatLikeStatus(catId, userId) { return request(`/cats/${catId}/like/status`, { params: { userId } }); }

// ====== Comments ======
function getComments(catId, sortBy, order, userId) {
  return request(`/cats/${catId}/comments/list`, { params: { sortBy, order, userId } });
}
function createComment(catId, data) { return request(`/cats/${catId}/comments`, { method: 'POST', data }); }
function deleteComment(commentId, userId) { return request(`/comments/${commentId}`, { method: 'DELETE', params: { userId } }); }
function toggleCommentLike(commentId, userId) { return request(`/comments/${commentId}/like`, { method: 'POST', data: { userId } }); }

// ====== Adopters ======
function adoptCat(catId, data) { return request(`/adopters/adopt/${catId}`, { method: 'POST', data }); }
function getAdoptersByCat(catId) { return request(`/adopters/cat/${catId}/list`); }
function getAllAdopters() { return request('/adopters/list'); }
function cancelAdoption(adopterId) { return request(`/adopters/${adopterId}`, { method: 'DELETE' }); }

// ====== Notifications ======
function getNotifications(adopterId) { return request(`/notifications/adopter/${adopterId}/list`); }
function getUnreadCount(adopterId) { return request(`/notifications/adopter/${adopterId}/unread/count`); }
function markNotificationRead(id) { return request(`/notifications/${id}/read`, { method: 'PUT' }); }
function markAllNotificationsRead(adopterId) { return request(`/notifications/adopter/${adopterId}/read/all`, { method: 'PUT' }); }

// ====== Tenants ======
function getTenants() { return request('/tenants/list'); }

function getFileUrl(url) {
  if (!url) return '';
  // 后端返回完整 URL（OSS 公共读）时直接使用，相对路径则拼后端代理访问
  if (/^https?:\/\//i.test(url)) return url;
  return config.UPLOAD_BASE + '/' + url;
}

module.exports = {
  request, uploadFile, getCats, getCatDetail,
  createUser,
  toggleCatLike, getCatLikeStatus,
  getComments, createComment, deleteComment, toggleCommentLike,
  adoptCat, getAdoptersByCat, getAllAdopters, cancelAdoption,
  getNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead,
  getTenants, getFileUrl,
};
