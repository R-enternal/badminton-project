// ========== 路由 & 全局状态 ==========
let currentPage = 'home';
let currentVenueSlot = null;
let currentVenueDate = formatDate(new Date());
let currentShopProducts = [];
let currentStock = {};

function formatDate(d) { return d.toISOString().split('T')[0]; }
function formatPrice(p) { return '¥' + (Number(p) || 0).toFixed(2); }
function statusText(s) {
  const m = {0:'待付款',1:'已付款',2:'已发货',3:'已收货',4:'已完成',5:'已取消'};
  return m[s] || '未知';
}

window.addEventListener('hashchange', routeFromHash);
window.addEventListener('DOMContentLoaded', () => { routeFromHash(); updateUserUI(); loadHomeStats(); });

function routeFromHash() {
  const hash = window.location.hash.replace('#', '') || 'home';
  if (['home','venue','coach','shop','cart','orders'].includes(hash)) {
    navigateTo(hash, false);
  }
}

function navigateTo(page, pushState = true) {
  currentPage = page;
  if (pushState) window.location.hash = page;
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const pageEl = document.getElementById('page-' + page);
  if (pageEl) pageEl.classList.add('active');
  document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
  const link = document.querySelector(`.nav-link[href="#${page}"]`);
  if (link) link.classList.add('active');
  document.getElementById('mobileMenu')?.classList.remove('show');
  window.scrollTo(0, 0);
  loadPageContent(page);
}

function loadPageContent(page) {
  switch(page) {
    case 'venue': loadVenuePage(); break;
    case 'coach': loadCoachPage(); break;
    case 'shop': loadShopPage(); break;
    case 'cart': loadCartPage(); break;
    case 'orders': loadOrders(); break;
    case 'home': loadHomeStats(); break;
  }
}

// ========== Toast ==========
function toast(msg, type = 'success') {
  const el = document.createElement('div');
  el.className = 'toast toast-' + type;
  el.textContent = msg;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 2500);
}

// ========== 用户 & 登录 ==========
function updateUserUI() {
  const user = getUser();
  document.getElementById('userName').textContent = user.nickname || '未登录';
  updateCartBadge();
}

async function updateCartBadge() {
  if (!isLogin()) { document.getElementById('cartBadge').textContent = '0'; return; }
  try {
    const cart = await API.cartList();
    document.getElementById('cartBadge').textContent = cart.length;
  } catch(e) { document.getElementById('cartBadge').textContent = '0'; }
}

function showLogin() {
  document.getElementById('loginCode').value = '';
  document.getElementById('loginModal').classList.add('show');
}

async function doLogin() {
  const code = document.getElementById('loginCode').value.trim();
  if (!code) { toast('请输入登录码', 'error'); return; }
  try {
    const res = await API.login(code);
    setToken(res.token);
    setUser(res.userInfo);
    closeModal('loginModal');
    updateUserUI();
    toast('登录成功');
    loadPageContent(currentPage);
  } catch(e) { toast(e.message, 'error'); }
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  updateUserUI();
  toast('已退出登录');
  navigateTo('home');
}

function requireLogin() {
  if (!isLogin()) { showLogin(); return false; }
  return true;
}

// ========== 弹窗 ==========
function closeModal(id) { document.getElementById(id).classList.remove('show'); }

function toggleUserMenu() {
  document.getElementById('userMenu').classList.toggle('show');
}
function closeUserMenu() { document.getElementById('userMenu').classList.remove('show'); }
function toggleMobileMenu() { document.getElementById('mobileMenu').classList.toggle('show'); }
function closeMobileMenu() { document.getElementById('mobileMenu').classList.remove('show'); }

// 点击其他区域关闭
document.addEventListener('click', (e) => {
  if (!e.target.closest('.nav-actions')) closeUserMenu();
});

// ========== 首页 ==========
async function loadHomeStats() {
  try {
    const [venues, coaches, products] = await Promise.all([
      API.venueList().catch(() => []),
      API.coachList().catch(() => []),
      API.products({}).catch(() => [])
    ]);
    document.getElementById('homeVenueCount').textContent = venues.length;
    document.getElementById('homeCoachCount').textContent = coaches.length;
    document.getElementById('homeProductCount').textContent = products.length;
  } catch(e) {}
}

// ========== 场地预约 ==========
async function loadVenuePage() {
  renderDateBar();
  await loadVenueSlots();
}

function renderDateBar() {
  const scroll = document.getElementById('dateScroll');
  const today = new Date();
  let html = '';
  for (let i = 0; i < 7; i++) {
    const d = new Date(today);
    d.setDate(d.getDate() + i);
    const dateStr = formatDate(d);
    const weekDay = ['周日','周一','周二','周三','周四','周五','周六'][d.getDay()];
    const active = dateStr === currentVenueDate ? ' active' : '';
    html += `<div class="date-chip${active}" onclick="selectDate('${dateStr}')">
      ${dateStr.slice(5)}<small>${weekDay}</small>
    </div>`;
  }
  scroll.innerHTML = html;
}

function changeDate(delta) {
  const d = new Date(currentVenueDate);
  d.setDate(d.getDate() + delta);
  currentVenueDate = formatDate(d);
  renderDateBar();
  loadVenueSlots();
}

function selectDate(dateStr) {
  currentVenueDate = dateStr;
  renderDateBar();
  loadVenueSlots();
}

async function loadVenueSlots() {
  const container = document.getElementById('venueList');
  container.innerHTML = '<div class="loading">加载中...</div>';
  try {
    const venues = await API.venueList();
    if (!venues.length) { container.innerHTML = '<div class="empty"><div class="empty-icon">🏟️</div><p>暂无场地</p></div>'; return; }
    let html = '';
    for (const venue of venues) {
      let slotsHtml = '';
      try {
        const slots = await API.venueSlots(venue.id, currentVenueDate);
        if (slots.length) {
          for (const slot of slots) {
            let cls = 'slot-chip available';
            let text = slot.startTime.slice(0,5) + '-' + slot.endTime.slice(0,5);
            let onclick = '';
            if (slot.status === 2) { cls = 'slot-chip booked'; text = '已约'; }
            else if (slot.status === 0) { cls = 'slot-chip closed'; text = '关闭'; onclick = ''; }
            else { onclick = `onclick="selectVenueSlot(${slot.id}, '${text}', ${slot.price}, '${venue.name}')"`; }
            slotsHtml += `<div class="${cls}" ${onclick}>${text}</div>`;
          }
        } else {
          slotsHtml = '<div style="font-size:12px;color:#94a3b8;grid-column:1/-1">暂无时段，请先生成</div>';
        }
      } catch(e) {
        slotsHtml = '<div style="font-size:12px;color:#94a3b8;grid-column:1/-1">加载失败</div>';
      }
      html += `<div class="venue-card">
        <div class="venue-card-header">
          <div><div class="venue-name">${venue.name}</div><div class="venue-location">${venue.location || ''}</div></div>
          <div class="venue-price">${formatPrice(venue.pricePerHour)}/小时</div>
        </div>
        <div class="slot-grid">${slotsHtml}</div>
      </div>`;
    }
    container.innerHTML = html;
  } catch(e) { container.innerHTML = '<div class="empty"><p>加载失败</p></div>'; }
}

function selectVenueSlot(slotId, timeText, price, venueName) {
  if (!requireLogin()) return;
  currentVenueSlot = { slotId, timeText, price, venueName };
  document.getElementById('venueModalBody').innerHTML = `
    <div style="text-align:center;padding:8px 0">
      <div style="font-size:32px;margin-bottom:12px">🏟️</div>
      <div style="font-size:18px;font-weight:700;margin-bottom:4px">${venueName}</div>
      <div style="font-size:14px;color:var(--text-secondary);margin-bottom:4px">${currentVenueDate} ${timeText}</div>
      <div style="font-size:24px;font-weight:700;color:#ef4444">${formatPrice(price)}</div>
    </div>`;
  document.getElementById('venueModal').classList.add('show');
}

async function confirmVenueBooking() {
  if (!currentVenueSlot) return;
  const btn = document.getElementById('venueConfirmBtn');
  btn.disabled = true; btn.textContent = '预约中...';
  try {
    await API.prepareBooking(currentVenueSlot.slotId);
    closeModal('venueModal');
    toast('预约成功！请在15分钟内完成支付');
    loadVenueSlots();
  } catch(e) { toast(e.message, 'error'); }
  finally { btn.disabled = false; btn.textContent = '确认预约'; }
}

// ========== 教练课程 ==========
async function loadCoachPage() {
  const container = document.getElementById('coachList');
  container.innerHTML = '<div class="loading">加载中...</div>';
  try {
    const coaches = await API.coachList();
    if (!coaches.length) { container.innerHTML = '<div class="empty"><div class="empty-icon">👨‍🏫</div><p>暂无教练</p></div>'; return; }
    container.innerHTML = coaches.map(c => `
      <div class="coach-card" onclick="showCoachDetail(${c.id})">
        <div class="coach-avatar">🏸</div>
        <div class="coach-name">${c.name}</div>
        <div class="coach-specialty">${c.specialty || ''}</div>
        <div class="coach-intro">${c.intro || ''}</div>
      </div>`).join('');
  } catch(e) { container.innerHTML = '<div class="empty"><p>加载失败</p></div>'; }
}

async function showCoachDetail(coachId) {
  if (!requireLogin()) return;
  document.getElementById('coachModal').classList.add('show');
  document.getElementById('coachModalTitle').textContent = '加载中...';
  document.getElementById('coachModalBody').innerHTML = '<div class="loading">加载中...</div>';
  try {
    const coach = await API.coachDetail(coachId);
    const courses = await API.coachCourses(coachId);
    const date = formatDate(new Date());
    const schedules = await API.coachSchedules(coachId, date, true);

    document.getElementById('coachModalTitle').textContent = coach.name;
    document.getElementById('coachModalBody').innerHTML = `
      <div class="coach-detail-header">
        <div class="coach-detail-avatar">🏸</div>
        <div>
          <h3>${coach.name}</h3>
          <p style="color:var(--text-secondary);font-size:14px">${coach.specialty || ''}</p>
          <p style="font-size:14px;margin-top:4px">${coach.intro || ''}</p>
        </div>
      </div>
      <h4 style="margin-bottom:12px">可预约课程</h4>
      <div class="course-list">
        ${courses.length ? courses.map(c => `
          <div class="course-item">
            <div class="course-info">
              <h4>${c.name}</h4>
              <div class="course-meta">${c.category} · ${c.durationMinutes}分钟 · ${c.description || ''}</div>
            </div>
            <div style="display:flex;align-items:center;gap:8px">
              <div class="course-price">${formatPrice(c.price)}</div>
              <select class="form-select" id="scheduleSelect_${c.id}" style="width:140px;font-size:12px">
                <option value="">选择时段</option>
                ${schedules.map(s => `<option value="${s.id}">${s.startTime.slice(0,5)}-${s.endTime.slice(0,5)}</option>`).join('')}
              </select>
              <button class="btn btn-primary btn-sm" onclick="bookCoachCourse(${coachId}, ${c.id})">预约</button>
            </div>
          </div>`).join('') : '<p style="color:var(--text-secondary)">暂无课程</p>'}
      </div>
      ${schedules.length ? `<p style="font-size:13px;color:var(--text-secondary);margin-top:8px">📅 ${date} 可约时段: ${schedules.map(s => s.startTime.slice(0,5)+'-'+s.endTime.slice(0,5)).join(', ')}</p>` : `<p style="font-size:13px;color:#ef4444;margin-top:8px">📅 ${date} 暂无可用时段</p>`}
    `;
  } catch(e) { document.getElementById('coachModalBody').innerHTML = '<div class="empty"><p>加载失败</p></div>'; }
}

async function bookCoachCourse(coachId, courseId) {
  const sel = document.getElementById('scheduleSelect_' + courseId);
  if (!sel || !sel.value) { toast('请选择时段', 'error'); return; }
  try {
    await API.prepareCoachBooking(Number(sel.value), courseId);
    toast('预约成功！请在15分钟内完成支付');
    closeModal('coachModal');
  } catch(e) { toast(e.message, 'error'); }
}

// ========== 商城 ==========
async function loadShopPage() {
  await loadCategories();
  await loadProducts();
}

async function loadCategories() {
  try {
    const cats = await API.categories();
    const sel = document.getElementById('categoryFilter');
    sel.innerHTML = '<option value="">全部分类</option>' + cats.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  } catch(e) {}
}

async function loadProducts() {
  const container = document.getElementById('productList');
  container.innerHTML = '<div class="loading">加载中...</div>';
  const params = {
    categoryId: document.getElementById('categoryFilter').value || undefined,
    keyword: document.getElementById('productSearch').value || undefined
  };
  try {
    const products = await API.products(params);
    currentShopProducts = products;
    if (!products.length) { container.innerHTML = '<div class="empty"><div class="empty-icon">🏪</div><p>暂无商品</p></div>'; return; }
    container.innerHTML = products.map(p => `
      <div class="product-card" onclick="showProductDetail(${p.id})">
        <div class="product-img">🏸</div>
        <div class="product-info">
          <div class="product-name">${p.name}</div>
          <div class="product-subtitle">${p.subtitle || p.categoryName || ''}</div>
          <div class="product-price-row">
            <span class="product-price">查看详情</span>
            <span style="font-size:12px;color:var(--text-secondary)">${p.status === 1 ? '在售' : '已下架'}</span>
          </div>
        </div>
      </div>`).join('');
  } catch(e) { container.innerHTML = '<div class="empty"><p>加载失败</p></div>'; }
}

async function showProductDetail(productId) {
  if (!requireLogin()) return;
  document.getElementById('productModal').classList.add('show');
  document.getElementById('productModalTitle').textContent = '加载中...';
  document.getElementById('productModalBody').innerHTML = '<div class="loading">加载中...</div>';
  try {
    const detail = await API.productDetail(productId);
    const spu = detail.spu;
    const skus = detail.skuList.filter(s => s.status === 1 && s.stock > 0);
    currentStock = {};

    document.getElementById('productModalTitle').textContent = spu.name;
    document.getElementById('productModalBody').innerHTML = `
      <div class="product-img" style="height:200px">🏸</div>
      <div style="padding:16px 0">
        <h3 style="margin-bottom:4px">${spu.name}</h3>
        <p style="color:var(--text-secondary);font-size:14px;margin-bottom:12px">${spu.subtitle || ''}</p>
        <div style="font-size:12px;color:var(--text-secondary);margin-bottom:16px">${spu.detail || ''}</div>
        <h4 style="margin-bottom:8px">规格选择</h4>
        <div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:16px">
          ${skus.length ? skus.map(s => {
            currentStock[s.id] = s;
            return `<div class="sku-option" style="padding:8px 16px;border:1px solid var(--border);border-radius:var(--radius-sm);cursor:pointer;font-size:13px;transition:var(--transition)" onmouseover="this.style.borderColor='var(--green)'" onmouseout="this.style.borderColor='var(--border)'" onclick="document.querySelectorAll('.sku-option').forEach(e=>e.style.background='');this.style.background='var(--green-light)';this.style.borderColor='var(--green)';document.getElementById('skuPrice').textContent='${formatPrice(s.price)}';document.getElementById('skuStock').textContent='库存: ${s.stock}件';document.getElementById('skuAddBtn').onclick=()=>addToCart(${s.id})">
              ${JSON.parse(s.specs.replace(/'/g, '"') || '{}').Color || JSON.parse(s.specs.replace(/'/g, '"') || '{}')['颜色'] || s.skuCode}
              </div>`;
          }).join('') : '<p style="color:var(--text-secondary)">暂无可用规格</p>'}
        </div>
        <div style="display:flex;justify-content:space-between;align-items:center;padding:16px;background:var(--bg);border-radius:var(--radius-sm)">
          <div><span style="font-size:24px;font-weight:700;color:#ef4444" id="skuPrice">--</span></div>
          <div style="font-size:12px;color:var(--text-secondary)" id="skuStock">请选择规格</div>
          <button class="btn btn-primary" id="skuAddBtn" disabled>加入购物车</button>
        </div>
      </div>`;
  } catch(e) { document.getElementById('productModalBody').innerHTML = '<div class="empty"><p>加载失败</p></div>'; }
}

async function addToCart(skuId, qty = 1) {
  try {
    await API.addCart(skuId, qty);
    toast('已加入购物车');
    updateCartBadge();
    closeModal('productModal');
  } catch(e) { toast(e.message, 'error'); }
}

// ========== 购物车 ==========
async function loadCartPage() {
  if (!requireLogin()) return;
  const container = document.getElementById('cartList');
  const footer = document.getElementById('cartFooter');
  try {
    const items = await API.cartList();
    if (!items.length) {
      container.innerHTML = '<div class="empty"><div class="empty-icon">🛒</div><p>购物车空空如也</p><button class="btn btn-primary" onclick="navigateTo(\'shop\')">去逛逛</button></div>';
      footer.style.display = 'none';
      return;
    }
    footer.style.display = 'block';
    let total = 0;
    let allSelected = true;
    container.innerHTML = items.map(item => {
      const checked = item.selected === 1;
      if (checked) total += Number(item.totalAmount || item.price * item.quantity);
      if (!checked) allSelected = false;
      return `<div class="cart-item">
        <input type="checkbox" class="cart-checkbox" ${checked ? 'checked' : ''} onchange="toggleCartItem(${item.id}, this.checked)">
        <div class="cart-img">🛍️</div>
        <div class="cart-info">
          <div class="name">${item.spuName}</div>
          <div class="specs">${item.skuSpecs ? (()=>{try{return JSON.stringify(JSON.parse(item.skuSpecs));}catch(e){return item.skuSpecs}})() : ''}</div>
          <div class="price">${formatPrice(item.price)}</div>
        </div>
        <div class="cart-qty">
          <button onclick="changeCartQty(${item.id}, ${item.quantity - 1})">−</button>
          <span>${item.quantity}</span>
          <button onclick="changeCartQty(${item.id}, ${item.quantity + 1})">+</button>
        </div>
        <div style="font-weight:700;color:#ef4444;white-space:nowrap">${formatPrice(item.totalAmount || item.price * item.quantity)}</div>
        <button class="cart-delete" onclick="removeCartItem(${item.id})">🗑️</button>
      </div>`;
    }).join('');
    document.getElementById('selectAll').checked = allSelected;
    document.getElementById('cartTotal').textContent = formatPrice(total);
    updateCartBadge();
  } catch(e) { container.innerHTML = '<div class="empty"><p>加载失败</p></div>'; }
}

async function changeCartQty(id, qty) {
  if (qty < 1) return removeCartItem(id);
  try { await API.updateCartQty(id, qty); loadCartPage(); } catch(e) { toast(e.message, 'error'); }
}

async function removeCartItem(id) {
  try { await API.deleteCart(id); toast('已删除'); loadCartPage(); } catch(e) { toast(e.message, 'error'); }
}

async function toggleCartItem(id, checked) {
  try { await API.selectCart(id, checked ? 1 : 0); loadCartPage(); } catch(e) { toast(e.message, 'error'); }
}

function toggleSelectAll() {
  const checked = document.getElementById('selectAll').checked;
  document.querySelectorAll('.cart-checkbox').forEach(cb => {
    if (cb.checked !== checked) { cb.checked = checked; }
  });
  document.querySelectorAll('.cart-item').forEach(item => {
    const cb = item.querySelector('.cart-checkbox');
    const id = item.querySelector('button[onclick^="removeCartItem"]')?.getAttribute('onclick')?.match(/\d+/)?.[0];
    if (id) toggleCartItem(Number(id), checked);
  });
  setTimeout(loadCartPage, 300);
}

function checkout() {
  if (!requireLogin()) return;
  const checkedIds = [];
  document.querySelectorAll('.cart-checkbox:checked').forEach(cb => {
    const match = cb.closest('.cart-item')?.querySelector('.cart-delete')?.getAttribute('onclick')?.match(/\d+/)?.[0];
    if (match) checkedIds.push(Number(match));
  });
  if (!checkedIds.length) { toast('请选择商品', 'error'); return; }
  window._checkoutIds = checkedIds;
  document.getElementById('checkoutTotal').textContent = document.getElementById('cartTotal').textContent;
  document.getElementById('checkoutModal').classList.add('show');
}

async function confirmOrder() {
  const data = {
    cartItemIds: window._checkoutIds,
    receiverName: document.getElementById('receiverName').value,
    receiverPhone: document.getElementById('receiverPhone').value,
    receiverAddress: document.getElementById('receiverAddress').value,
    remark: document.getElementById('orderRemark').value
  };
  if (!data.receiverName || !data.receiverPhone || !data.receiverAddress) {
    toast('请填写收货信息', 'error'); return;
  }
  try {
    await API.createOrder(data);
    closeModal('checkoutModal');
    toast('下单成功！');
    loadCartPage();
    navigateTo('orders');
  } catch(e) { toast(e.message, 'error'); }
}

// ========== 订单 ==========
let currentOrderStatus = '';
async function loadOrders(status) {
  if (!requireLogin()) return;
  currentOrderStatus = status != null ? status : '';
  // update tabs
  document.querySelectorAll('#orderTabs .tab').forEach(t => t.classList.remove('active'));
  const activeTab = document.querySelector(`#orderTabs .tab[data-status="${status != null ? status : ''}"]`);
  if (activeTab) activeTab.classList.add('active');

  const container = document.getElementById('orderList');
  container.innerHTML = '<div class="loading">加载中...</div>';
  try {
    const orders = await API.myOrders(status != null ? status : undefined);
    if (!orders.length) { container.innerHTML = '<div class="empty"><div class="empty-icon">📋</div><p>暂无订单</p></div>'; return; }
    container.innerHTML = orders.map(o => `
      <div class="order-card">
        <div class="order-header">
          <span class="order-no">订单号: ${o.orderNo}</span>
          <span class="order-status status-${o.status}">${statusText(o.status)}</span>
        </div>
        <div class="order-items">
          ${(o.items || []).map(i => `<div class="order-item">
            <span>🛍️</span>
            <span>${i.spuName} ${i.skuSpecs ? (()=>{try{const s=JSON.parse(i.skuSpecs);return Object.values(s).join('/');}catch(e){return ''}})() : ''} x${i.quantity}</span>
            <span style="font-weight:600">${formatPrice(i.totalAmount)}</span>
          </div>`).join('')}
        </div>
        <div class="order-total">合计: ${formatPrice(o.totalAmount)}</div>
        ${o.status === 0 ? `<div class="order-actions"><button class="btn btn-danger btn-sm" onclick="cancelShopOrder(${o.id})">取消订单</button></div>` : ''}
        ${o.status === 2 ? `<div class="order-actions"><button class="btn btn-primary btn-sm" onclick="receiveShopOrder(${o.id})">确认收货</button></div>` : ''}
      </div>`).join('');
  } catch(e) { container.innerHTML = '<div class="empty"><p>加载失败</p></div>'; }
}

async function cancelShopOrder(id) {
  if (!confirm('确认取消该订单？')) return;
  try { await API.cancelOrder(id); toast('已取消'); loadOrders(currentOrderStatus); } catch(e) { toast(e.message, 'error'); }
}

async function receiveShopOrder(id) {
  try { await API.receiveOrder(id); toast('已确认收货'); loadOrders(currentOrderStatus); } catch(e) { toast(e.message, 'error'); }
}
