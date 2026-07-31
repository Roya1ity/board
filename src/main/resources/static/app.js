const state = {
  token: localStorage.getItem("accessToken") || "",
  user: JSON.parse(localStorage.getItem("user") || "null"),
  selectedBoard: null,
  selectedPost: null,
  commentPage: 0,
  editingPostId: null,
};

const els = {};

document.addEventListener("DOMContentLoaded", init);

async function init() {
  [
    "notice", "loginMessage", "userBadge", "logoutBtn", "boardList", "adminBoardList",
    "selectedBoardLabel", "postList", "postPager", "postDetail", "postDetailTitle", "postMeta", "postMetaActions",
    "commentList", "commentPager", "commentMeta", "postForm", "commentForm",
    "notifyWrap", "notifyBtn", "notifyBadge", "notifyPopup", "notifyList", "closeNotifyBtn",
  ].forEach((id) => { els[id] = document.getElementById(id); });

  bindEvents();
  await restoreSession();
  updateSessionUi();
  updateActionButtons();
  showView("board");
  await loadBoards();
}

function bindEvents() {
  document.querySelectorAll(".tab").forEach((tab) => {
    tab.addEventListener("click", () => showView(tab.dataset.view));
  });
  document.getElementById("refreshBoardsBtn").addEventListener("click", loadBoards);
  document.getElementById("openPostFormBtn").addEventListener("click", requireLoginThen(openPostForm));
  document.getElementById("cancelPostFormBtn").addEventListener("click", closePostForm);
  document.getElementById("openCommentFormBtn").addEventListener("click", requireLoginThen(() => openCommentForm(null)));
  document.getElementById("cancelCommentFormBtn").addEventListener("click", closeCommentForm);
  document.getElementById("postForm").addEventListener("submit", submitPost);
  document.getElementById("commentForm").addEventListener("submit", submitComment);
  document.getElementById("boardForm").addEventListener("submit", submitBoard);
  els.notifyBtn.addEventListener("click", toggleNotifications);
  els.closeNotifyBtn.addEventListener("click", () => els.notifyPopup.classList.add("hidden"));
  els.logoutBtn.addEventListener("click", logout);
}

async function restoreSession(force = false) {
  if (state.token && !force) return true;
  try {
    const res = await fetch("/api/auth/reissue", { method: "POST", credentials: "include" });
    if (!res.ok) {
      if (force) clearLogin();
      return false;
    }
    saveLogin(await res.json());
    updateSessionUi();
    return true;
  } catch {
    if (force) clearLogin();
    return false;
  }
}

function showView(name) {
  if (name === "admin" && !isAdmin()) {
    requireLogin();
    return;
  }
  document.querySelectorAll(".view").forEach((view) => view.classList.add("hidden"));
  document.getElementById(`${name}View`).classList.remove("hidden");
  document.querySelectorAll(".tab").forEach((tab) => tab.classList.toggle("is-active", tab.dataset.view === name));
}

function updateSessionUi() {
  const loggedIn = isLoggedIn();
  els.userBadge.textContent = loggedIn ? (state.user?.nick || state.user?.email || "로그인됨") : "로그인 전";
  els.logoutBtn.classList.toggle("hidden", !loggedIn);
  els.notifyWrap.classList.toggle("hidden", !loggedIn);
  document.querySelectorAll(".admin-only").forEach((el) => el.classList.toggle("hidden", !isAdmin()));
  if (loggedIn) loadUnreadCount();
}

function isLoggedIn() {
  return Boolean(state.token);
}

function isAdmin() {
  const role = String(state.user?.role || "").toUpperCase();
  const email = String(state.user?.email || "").toUpperCase();
  const nick = String(state.user?.nick || "").toUpperCase();
  return role === "ADMIN" || email === "ADMIN" || nick === "ADMIN";
}

function saveLogin(data) {
  const token = data.accessToken?.startsWith("Bearer ") ? data.accessToken : `Bearer ${data.accessToken}`;
  state.token = token;
  state.user = { id: data.id, email: data.email, nick: data.nick, role: data.role };
  localStorage.setItem("accessToken", token);
  localStorage.setItem("user", JSON.stringify(state.user));
}

function clearLogin() {
  state.token = "";
  state.user = null;
  localStorage.removeItem("accessToken");
  localStorage.removeItem("user");
}

async function api(path, options = {}, retried = false) {
  const { authRequired = false, ...fetchOptions } = options;
  const headers = new Headers(options.headers || {});
  if (state.token) headers.set("Authorization", state.token);
  const res = await fetch(path, { ...fetchOptions, headers, credentials: "include" });
  if (res.status === 401) {
    if (!retried && path !== "/api/auth/reissue" && await restoreSession(true)) {
      return api(path, options, true);
    }
    clearLogin();
    updateSessionUi();
    if (!authRequired && !retried) {
      return api(path, { ...options, authRequired: false }, true);
    }
    if (authRequired) requireLogin();
    throw new Error("로그인이 필요합니다");
  }
  if (!res.ok) throw new Error(await readError(res));
  if (res.status === 204) return null;
  const contentType = res.headers.get("content-type") || "";
  const contentLength = res.headers.get("content-length");
  if (contentLength === "0" || !contentType.includes("application/json")) return null;
  return res.json();
}

async function readError(res) {
  try {
    const data = await res.json();
    return data.msg || data.message || data.code || "요청 처리 중 오류가 발생했습니다.";
  } catch {
    return "요청 처리 중 오류가 발생했습니다.";
  }
}

async function loadBoards() {
  try {
    const boards = await api("/api/board/all", { authRequired: false });
    renderBoards(boards);
    renderAdminBoards(boards);
  } catch (err) {
    showNotice(err.message);
  }
}

function renderBoards(boards) {
  if (!boards.length) {
    els.boardList.innerHTML = `<div class="empty-state">게시판이 없습니다.</div>`;
    return;
  }
  els.boardList.innerHTML = boards.map((board) => `
    <button class="board-card ${state.selectedBoard?.id === board.id ? "is-active" : ""}" data-board-id="${board.id}">
      <h3>${escapeHtml(board.name)}</h3>
      <p>${escapeHtml(board.description || "설명이 없습니다.")}</p>
      <div class="meta">${formatDate(board.createAt || board.createdAt)}</div>
    </button>
  `).join("");
  els.boardList.querySelectorAll("[data-board-id]").forEach((card) => {
    card.addEventListener("click", () => selectBoard(boards.find((board) => String(board.id) === card.dataset.boardId)));
  });
}

function renderAdminBoards(boards) {
  els.adminBoardList.innerHTML = boards.length
    ? boards.map((board) => `
      <div class="list-item admin-board-item">
        <div>
          <h3>${escapeHtml(board.name)}</h3>
          <p>${escapeHtml(board.description || "")}</p>
        </div>
        <button class="button danger" type="button" data-delete-board-id="${board.id}">삭제</button>
      </div>
    `).join("")
    : `<div class="empty-state">관리할 게시판이 없습니다.</div>`;
  els.adminBoardList.querySelectorAll("[data-delete-board-id]").forEach((button) => {
    button.addEventListener("click", () => deleteBoard(button.dataset.deleteBoardId));
  });
}

async function selectBoard(board) {
  state.selectedBoard = board;
  state.selectedPost = null;
  els.selectedBoardLabel.textContent = board.name;
  resetPostDetail();
  updateActionButtons();
  els.commentList.innerHTML = `<div class="empty-state">포스트를 선택하면 댓글이 표시됩니다.</div>`;
  els.commentPager.innerHTML = "";
  closeCommentForm();
  renderBoards(await api("/api/board/all", { authRequired: false }));
  await loadPosts(0);
}

async function loadPosts(page = 0) {
  if (!state.selectedBoard) return;
  try {
    const data = await api(`/api/post/${state.selectedBoard.id}/all?page=${page}&size=10`, { authRequired: false });
    const posts = normalizePage(data);
    renderPosts(posts.items);
    renderPager(els.postPager, posts, loadPosts);
  } catch (err) {
    showNotice(err.message);
  }
}

function renderPosts(posts) {
  if (!posts.length) {
    els.postList.innerHTML = `<div class="empty-state">작성된 글이 없습니다.</div>`;
    return;
  }
  els.postList.innerHTML = posts.map((post) => `
    <button class="list-item ${state.selectedPost?.id === post.id ? "is-active" : ""}" data-post-id="${post.id}">
      <h3>${escapeHtml(post.title)}</h3>
      <div class="meta">
        <span>${escapeHtml(post.user || "작성자 없음")}</span>
        <span>조회 ${post.viewCount ?? 0}</span>
        <span>${formatDate(post.createAt || post.createdAt)}</span>
      </div>
    </button>
  `).join("");
  els.postList.querySelectorAll("[data-post-id]").forEach((item) => {
    item.addEventListener("click", () => selectPost(item.dataset.postId));
  });
}

async function selectPost(postId) {
  try {
    state.selectedPost = await api(`/api/post/${postId}`, { authRequired: false });
    renderPostDetail(state.selectedPost);
    updateActionButtons();
    await loadComments(0);
  } catch (err) {
    showNotice(err.message);
  }
}

function renderPostDetail(post) {
  els.postDetailTitle.textContent = post.title || "포스트 내용";
  els.postMeta.textContent = `${post.board || ""} / ${post.user || ""} / 조회 ${post.viewCount ?? 0}`;
  renderPostActions(post);
  els.postDetail.classList.remove("empty");
  const images = Array.isArray(post.images) && post.images.length
    ? `<div class="post-images">${post.images.map((img) => `<img src="${escapeAttr(img.url)}" alt="${escapeAttr(img.originalName || post.title)}">`).join("")}</div>`
    : "";
  els.postDetail.innerHTML = `<p>${escapeHtml(post.body)}</p>${images}`;
}

function resetPostDetail() {
  els.postDetailTitle.textContent = "포스트 내용";
  els.postDetail.textContent = "선택된 포스트가 없습니다.";
  els.postDetail.classList.add("empty");
  els.postMeta.textContent = "글을 선택하세요.";
  els.postMetaActions.innerHTML = "";
}

function updateActionButtons() {
  document.getElementById("openPostFormBtn").classList.toggle("hidden", !state.selectedBoard);
  document.getElementById("openCommentFormBtn").classList.toggle("hidden", !state.selectedPost);
}

function renderPostActions(post) {
  els.postMetaActions.innerHTML = post.canEdit || post.canDelete
    ? `${post.canEdit ? `<button class="button ghost small" type="button" data-edit-post="${post.id}">수정</button>` : ""}
       ${post.canDelete ? `<button class="button danger small" type="button" data-delete-post="${post.id}">삭제</button>` : ""}`
    : "";
  const editButton = els.postMetaActions.querySelector("[data-edit-post]");
  const deleteButton = els.postMetaActions.querySelector("[data-delete-post]");
  if (editButton) editButton.addEventListener("click", () => editPost(post));
  if (deleteButton) deleteButton.addEventListener("click", () => deletePost(post.id));
}

async function loadComments(page = 0) {
  if (!state.selectedPost) return;
  state.commentPage = page;
  try {
    const data = await api(`/api/comment/${state.selectedPost.id}/list?page=${page}&size=10`, { authRequired: false });
    const comments = normalizePage(data);
    els.commentMeta.textContent = state.selectedPost.title + "에 달린 댓글";
    renderComments(comments.items);
    renderPager(els.commentPager, comments, loadComments);
  } catch (err) {
    showNotice(err.message);
  }
}

function renderComments(comments) {
  if (!comments.length) {
    els.commentList.innerHTML = `<div class="empty-state">댓글이 없습니다.</div>`;
    return;
  }
  els.commentList.innerHTML = comments.map((comment) => renderComment(comment, false)).join("");
  els.commentList.querySelectorAll("[data-reply-id]").forEach((button) => {
    button.addEventListener("click", requireLoginThen(() => openCommentForm(button.dataset.replyId)));
  });
  els.commentList.querySelectorAll("[data-edit-comment]").forEach((button) => {
    button.addEventListener("click", () => editComment(button.dataset.editComment));
  });
  els.commentList.querySelectorAll("[data-delete-comment]").forEach((button) => {
    button.addEventListener("click", () => deleteComment(button.dataset.deleteComment));
  });
}

function renderComment(comment, reply) {
  const children = Array.isArray(comment.children) ? comment.children : [];
  return `
    <div class="comment ${reply ? "reply" : ""}">
      <div class="comment-head">
        <strong>${escapeHtml(comment.author || "작성자 없음")}</strong>
          <div class="comment-meta-actions">
            <span class="muted">${formatDate(comment.createdAt)}</span>
            <div class="comment-owner-actions">
            ${comment.canEdit ? `<button class="button ghost small" type="button" data-edit-comment="${comment.id}">수정</button>` : ""}
            ${comment.canDelete ? `<button class="button danger small" type="button" data-delete-comment="${comment.id}">삭제</button>` : ""}
          </div>
        </div>
      </div>
      <p>${escapeHtml(comment.content || "")}</p>
      <div class="comment-actions">
        <button class="link-button" type="button" data-reply-id="${comment.id}">답글</button>
      </div>
    </div>
    ${children.map((child) => renderComment(child, true)).join("")}
  `;
}

async function editPost(post) {
  if (!await ensureAuthenticated()) return;
  state.editingPostId = post.id;
  document.getElementById("postTitle").value = post.title || "";
  document.getElementById("postBody").value = post.body || "";
  document.getElementById("postImages").value = "";
  els.postForm.classList.remove("hidden");
  document.getElementById("postTitle").focus();
}

async function deletePost(postId) {
  if (!await ensureAuthenticated()) return;
  if (!window.confirm("게시글을 삭제하시겠습니까?")) return;

  try {
    await api(`/api/post/${postId}/delete`, { method: "DELETE", authRequired: true });
    showNotice("게시글이 삭제되었습니다.");
  } catch (err) {
    showNotice(err.message);
  }
}

async function editComment(commentId) {
  if (!await ensureAuthenticated()) return;
  const content = window.prompt("댓글을 수정하세요.");
  if (content === null) return;

  try {
    await api(`/api/comment/${commentId}/update`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content: content.trim(), parentId: null }),
      authRequired: true,
    });
    await loadComments(state.commentPage);
    showNotice("댓글이 수정되었습니다.");
  } catch (err) {
    showNotice(err.message);
  }
}

async function deleteComment(commentId) {
  if (!await ensureAuthenticated()) return;
  if (!window.confirm("댓글 삭제 권한을 확인하시겠습니까?")) return;

  try {
    await api(`/api/comment/${commentId}/delete`, { method: "DELETE", authRequired: true });
    showNotice("댓글 삭제 권한 확인이 완료되었습니다.");
  } catch (err) {
    showNotice(err.message);
  }
}

function openPostForm() {
  if (!state.selectedBoard) {
    showNotice("게시판을 먼저 선택하세요.");
    return;
  }
  state.editingPostId = null;
  document.getElementById("postForm").reset();
  els.postForm.classList.remove("hidden");
}

function closePostForm() {
  state.editingPostId = null;
  document.getElementById("postForm").reset();
  els.postForm.classList.add("hidden");
}

async function submitPost(event) {
  event.preventDefault();
  if (!state.selectedBoard) return showNotice("게시판을 먼저 선택하세요.");
  if (!await ensureAuthenticated()) return;

  const post = {
    title: document.getElementById("postTitle").value.trim(),
    body: document.getElementById("postBody").value.trim(),
  };
  const formData = new FormData();
  formData.append("post", new Blob([JSON.stringify(post)], { type: "application/json" }));
  Array.from(document.getElementById("postImages").files).slice(0, 3).forEach((file) => formData.append("images", file));

  try {
    if (state.editingPostId) {
      state.selectedPost = await api(`/api/post/${state.editingPostId}/update`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(post),
        authRequired: true,
      });
      renderPostDetail(state.selectedPost);
      showNotice("게시글이 수정되었습니다.");
    } else {
      await api(`/api/post/${state.selectedBoard.id}/new`, { method: "POST", body: formData, authRequired: true });
      showNotice("글이 등록되었습니다.");
    }
    event.target.reset();
    state.editingPostId = null;
    els.postForm.classList.add("hidden");
    await loadPosts(0);
  } catch (err) {
    showNotice(err.message);
  }
}

async function submitComment(event) {
  event.preventDefault();
  if (!state.selectedPost) return showNotice("포스트를 먼저 선택하세요.");
  if (!await ensureAuthenticated()) return;

  const req = {
    content: document.getElementById("commentContent").value.trim(),
    parentId: document.getElementById("parentCommentId").value || null,
  };

  try {
    await api(`/api/comment/${state.selectedPost.id}/new`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(req),
      authRequired: true,
    });
    closeCommentForm();
    await loadComments();
    showNotice("댓글이 등록되었습니다.");
  } catch (err) {
    showNotice(err.message);
  }
}

async function submitBoard(event) {
  event.preventDefault();
  if (!await ensureAuthenticated()) return;

  const req = {
    name: document.getElementById("boardName").value.trim(),
    description: document.getElementById("boardDescription").value.trim(),
  };

  try {
    await api("/api/board/new", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(req),
      authRequired: true,
    });
    event.target.reset();
    await loadBoards();
    showNotice("게시판이 생성되었습니다.");
  } catch (err) {
    showNotice(err.message);
  }
}

async function deleteBoard(boardId) {
  if (!await ensureAuthenticated()) return;
  if (!window.confirm("게시판을 삭제하시겠습니까?")) return;

  try {
    await api(`/api/board/${boardId}/delete`, { method: "DELETE", authRequired: true });
    if (state.selectedBoard && String(state.selectedBoard.id) === String(boardId)) {
      state.selectedBoard = null;
      state.selectedPost = null;
      els.selectedBoardLabel.textContent = "게시판을 선택하세요.";
      els.postList.innerHTML = "";
      els.postPager.innerHTML = "";
      resetPostDetail();
      updateActionButtons();
      els.commentList.innerHTML = "";
      els.commentPager.innerHTML = "";
    }
    await loadBoards();
    showNotice("게시판이 삭제되었습니다.");
  } catch (err) {
    showNotice(err.message);
  }
}

async function loadUnreadCount() {
  if (!isLoggedIn()) return;
  try {
    const data = await api("/api/notify/unreads", { authRequired: true });
    const count = Number(data.count || 0);
    els.notifyBadge.textContent = count > 99 ? "99+" : String(count);
    els.notifyBadge.classList.toggle("hidden", count === 0);
  } catch {
    els.notifyBadge.classList.add("hidden");
  }
}

async function toggleNotifications() {
  if (!await ensureAuthenticated()) return;
  const willOpen = els.notifyPopup.classList.contains("hidden");
  els.notifyPopup.classList.toggle("hidden", !willOpen);
  if (willOpen) await loadNotifications();
}

async function loadNotifications() {
  try {
    const data = await api("/api/notify/list?page=0&size=10", { authRequired: true });
    const notifications = normalizePage(data).items;
    renderNotifications(notifications);
    await loadUnreadCount();
  } catch (err) {
    els.notifyList.innerHTML = `<div class="empty-state">${escapeHtml(err.message)}</div>`;
  }
}

function renderNotifications(notifications) {
  if (!notifications.length) {
    els.notifyList.innerHTML = `<div class="empty-state">알림이 없습니다.</div>`;
    return;
  }

  els.notifyList.innerHTML = notifications.map((item) => `
    <button class="notify-item ${item.read ? "" : "unread"}" type="button" data-notify-id="${item.id}" data-post-id="${item.postId}">
      <strong>${escapeHtml(item.message || "새 알림")}</strong>
      <div class="meta">
        <span>${escapeHtml(item.actor || "")}</span>
        <span>${formatDate(item.createdAt)}</span>
      </div>
    </button>
  `).join("");

  els.notifyList.querySelectorAll("[data-notify-id]").forEach((button) => {
    button.addEventListener("click", () => openNotification(button.dataset.notifyId, button.dataset.postId));
  });
}

async function openNotification(notificationId, postId) {
  try {
    await api(`/api/notify/${notificationId}/read`, { method: "PUT", authRequired: true });
    els.notifyPopup.classList.add("hidden");
    showView("board");
    state.selectedPost = await api(`/api/post/${postId}`, { authRequired: false });
    state.selectedBoard = null;
    els.selectedBoardLabel.textContent = state.selectedPost.board || "알림에서 이동한 게시글";
    renderPostDetail(state.selectedPost);
    updateActionButtons();
    await loadComments(0);
    await loadUnreadCount();
  } catch (err) {
    showNotice(err.message);
  }
}

function openCommentForm(parentId) {
  if (!state.selectedPost) {
    showNotice("포스트를 먼저 선택하세요.");
    return;
  }
  document.getElementById("parentCommentId").value = parentId || "";
  document.getElementById("commentContent").placeholder = parentId ? "답글 내용" : "댓글 내용";
  els.commentForm.classList.remove("hidden");
  document.getElementById("commentContent").focus();
}

function closeCommentForm() {
  document.getElementById("commentForm").reset();
  document.getElementById("parentCommentId").value = "";
  els.commentForm.classList.add("hidden");
}

function renderPager(container, page, loader) {
  if (page.totalPages <= 1) {
    container.innerHTML = "";
    return;
  }
  container.innerHTML = Array.from({ length: page.totalPages }, (_, i) =>
    `<button class="button ${i === page.number ? "accent" : "ghost"}" data-page="${i}" type="button">${i + 1}</button>`
  ).join("");
  container.querySelectorAll("[data-page]").forEach((button) => {
    button.addEventListener("click", () => loader(Number(button.dataset.page)));
  });
}

function normalizePage(data) {
  return {
    items: data.content || [],
    number: data.page?.number ?? data.number ?? 0,
    totalPages: data.page?.totalPages ?? data.totalPages ?? 1,
  };
}

function requireLoginThen(action) {
  return async () => {
    if (!await ensureAuthenticated()) return;
    action();
  };
}

async function ensureAuthenticated() {
  if (isLoggedIn()) return true;
  if (await restoreSession(true)) return true;
  requireLogin();
  return false;
}

function requireLogin() {
  els.loginMessage.textContent = "로그인이 필요합니다";
  showNotice("로그인이 필요합니다");
  showView("login");
}

async function logout() {
  try {
    await fetch("/api/auth/logout", { method: "POST", credentials: "include" });
  } finally {
    clearLogin();
    updateSessionUi();
    showView("login");
  }
}

function showNotice(message) {
  els.notice.textContent = message;
  els.notice.classList.remove("hidden");
  window.setTimeout(() => els.notice.classList.add("hidden"), 3500);
}

function formatDate(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString("ko-KR", { dateStyle: "short", timeStyle: "short" });
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
  return escapeHtml(value).replaceAll("`", "&#096;");
}
