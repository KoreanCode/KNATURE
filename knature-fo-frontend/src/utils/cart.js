// 장바구니 — localStorage 기반 (비회원도 담기 가능, 주문 시 로그인 필요)
const KEY = 'knature_cart';

function read() {
  try { return JSON.parse(localStorage.getItem(KEY)) || []; }
  catch { return []; }
}

function write(items) {
  localStorage.setItem(KEY, JSON.stringify(items));
  window.dispatchEvent(new Event('cart-changed')); // 헤더 배지 갱신용
}

export function getCart() {
  return read();
}

export function cartCount() {
  return read().reduce((sum, it) => sum + it.quantity, 0);
}

/** item: {productId, optionId|null, name, optionName|null, unitPrice, imageUrl|null, quantity} */
export function addToCart(item) {
  const items = read();
  const found = items.find((it) => it.productId === item.productId && it.optionId === item.optionId);
  if (found) {
    found.quantity += item.quantity;
  } else {
    items.push(item);
  }
  write(items);
}

export function updateQuantity(productId, optionId, quantity) {
  const items = read();
  const found = items.find((it) => it.productId === productId && it.optionId === optionId);
  if (found) {
    found.quantity = Math.max(1, quantity);
    write(items);
  }
}

export function removeItems(keys) {
  // keys: [{productId, optionId}]
  const items = read().filter((it) => !keys.some((k) => k.productId === it.productId && k.optionId === it.optionId));
  write(items);
}

export function clearCart() {
  write([]);
}
