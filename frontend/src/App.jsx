import { useCallback, useEffect, useState } from "react";
import { api, authStore, json, normalizePage, restoreSession } from "./api.js";

const emptyPage = { items: [], page: 0, totalPages: 0 };

function Pager({ value, onChange }) {
  if (value.totalPages < 2) return null;
  return <nav className="pager" aria-label="페이지 이동">
    {Array.from({ length: value.totalPages }, (_, page) =>
      <button className={page === value.page ? "active" : ""} key={page} onClick={() => onChange(page)}>{page + 1}</button>)}
  </nav>;
}

function ReactionButtons({ target, id, reaction, onChanged, onError = () => {} }) {
  const react = async (type) => {
    try {
      const next = await api(`/api/reation/${target}/${id}`, json("POST", { type }));
      onChanged(next);
    } catch (e) { onError(e.message); }
  };
  return <div className="reactions">
    <button disabled={!authStore.token()} className={reaction.myReaction === "LIKE" ? "selected" : ""} onClick={() => react("LIKE")}>좋아요 {reaction.likeCount ?? reaction.like ?? 0}</button>
    <button disabled={!authStore.token()} className={reaction.myReaction === "DISLIKE" ? "selected" : ""} onClick={() => react("DISLIKE")}>싫어요 {reaction.dislikeCount ?? reaction.dislike ?? 0}</button>
  </div>;
}

function CommentItem({ comment, postId, onReload, onError }) {
  const [reaction, setReaction] = useState(comment);
  const remove = async () => {
    if (!confirm("댓글을 삭제할까요?")) return;
    try { await api(`/api/comment/${comment.id}/delete`, { method: "DELETE" }); onReload(); } catch (e) { onError(e.message); }
  };
  const edit = async () => {
    const content = prompt("댓글 내용", comment.content);
    if (!content?.trim()) return;
    try { await api(`/api/comment/${comment.id}/update`, json("PUT", { content })); onReload(); } catch (e) { onError(e.message); }
  };
  const reply = async () => {
    const content = prompt("답글 내용");
    if (!content?.trim()) return;
    try { await api(`/api/comment/${postId}/new`, json("POST", { content, parentId: comment.id })); onReload(); } catch (e) { onError(e.message); }
  };
  return <article className="comment">
    <header><strong>{comment.author}</strong><time>{formatDate(comment.createdAt)}</time></header>
    <p>{comment.content}</p>
    <div className="row">
      <ReactionButtons target="comment" id={comment.id} reaction={reaction} onChanged={(next) => setReaction({ ...reaction, ...next })} onError={onError} />
      {authStore.token() && !comment.deleted && <button onClick={reply}>답글</button>}
      {comment.canEdit && <button onClick={edit}>수정</button>}
      {comment.canDelete && <button className="danger" onClick={remove}>삭제</button>}
    </div>
    {comment.children?.map((child) => <div className="reply" key={child.id}><CommentItem comment={child} postId={postId} onReload={onReload} onError={onError} /></div>)}
  </article>;
}

function App() {
  const [user, setUser] = useState(authStore.user());
  const [boards, setBoards] = useState([]);
  const [board, setBoard] = useState(null);
  const [posts, setPosts] = useState(emptyPage);
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState(emptyPage);
  const [notice, setNotice] = useState("");
  const [loading, setLoading] = useState(true);

  const run = useCallback(async (work) => {
    try { setNotice(""); return await work(); } catch (e) { setNotice(e.message); return null; }
  }, []);

  const loadBoards = useCallback(() => run(async () => setBoards(await api("/api/board/all"))), [run]);
  const loadPosts = useCallback((page = 0) => board && run(async () => {
    setPosts(normalizePage(await api(`/api/post/${board.id}/all?page=${page}&size=10`)));
  }), [board, run]);
  const loadComments = useCallback((page = 0) => post && run(async () => {
    setComments(normalizePage(await api(`/api/comment/${post.id}/list?page=${page}&size=10`)));
  }), [post, run]);

  useEffect(() => { (async () => { setUser(await restoreSession()); await loadBoards(); setLoading(false); })(); }, [loadBoards]);
  useEffect(() => { if (board) loadPosts(); }, [board, loadPosts]);
  useEffect(() => { if (post) loadComments(); }, [post, loadComments]);

  const selectPost = (id) => run(async () => setPost(await api(`/api/post/${id}`)));
  const logout = () => run(async () => {
    await api("/api/auth/logout", { method: "POST" }); authStore.clear(); setUser(null); setPost(null);
  });
  const createPost = async (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const body = new FormData();
    body.append("post", new Blob([JSON.stringify({ title: form.get("title"), body: form.get("body") })], { type: "application/json" }));
    [...event.currentTarget.images.files].forEach((image) => body.append("images", image));
    const created = await run(() => api(`/api/post/${board.id}/new`, { method: "POST", body }));
    if (created) { event.currentTarget.reset(); await loadPosts(); await selectPost(created.id); }
  };
  const createComment = async (event) => {
    event.preventDefault();
    const content = new FormData(event.currentTarget).get("content");
    const created = await run(() => api(`/api/comment/${post.id}/new`, json("POST", { content, parentId: null })));
    if (created) { event.currentTarget.reset(); await loadComments(); }
  };
  const removePost = async () => {
    if (!confirm("게시글을 삭제할까요?")) return;
    const done = await run(() => api(`/api/post/${post.id}/delete`, { method: "DELETE" }));
    if (done !== null) { setPost(null); setComments(emptyPage); await loadPosts(); }
  };
  const editPost = async () => {
    const title = prompt("제목", post.title);
    if (!title?.trim()) return;
    const body = prompt("내용", post.body);
    if (!body?.trim()) return;
    const updated = await run(() => api(`/api/post/${post.id}/update`, json("PUT", { title, body })));
    if (updated) { await loadPosts(posts.page); await selectPost(post.id); }
  };

  if (loading) return <main className="center">불러오는 중…</main>;
  return <>
    <header className="topbar">
      <a className="brand" href="/">Board</a>
      <div className="session">
        {user ? <><span>{user.nick || user.email}</span><button onClick={logout}>로그아웃</button></> : <>
          <a className="kakao" href="/oauth2/authorization/kakao">카카오 로그인</a>
          <a href="/oauth2/authorization/google">Google 로그인</a>
        </>}
      </div>
    </header>
    <main className="layout">
      {notice && <div className="notice" role="alert">{notice}<button onClick={() => setNotice("")}>×</button></div>}
      <aside className="panel boards">
        <div className="title"><h2>게시판</h2><button onClick={loadBoards}>새로고침</button></div>
        {boards.map((item) => <button key={item.id} className={board?.id === item.id ? "card selected" : "card"} onClick={() => { setBoard(item); setPost(null); }}>{item.name}<small>{item.description}</small></button>)}
      </aside>
      <section className="panel posts">
        <h2>{board?.name || "게시판을 선택하세요"}</h2>
        {user && board && <form className="form" onSubmit={createPost}>
          <input name="title" maxLength="120" placeholder="제목" required />
          <textarea name="body" placeholder="내용" required />
          <input name="images" type="file" accept="image/*" multiple />
          <button type="submit">글 등록</button>
        </form>}
        {posts.items.map((item) => <button className={post?.id === item.id ? "card selected" : "card"} key={item.id} onClick={() => selectPost(item.id)}>
          {item.title}<small>{item.user} · 조회 {item.viewCount} · {formatDate(item.createAt)}</small>
        </button>)}
        <Pager value={posts} onChange={loadPosts} />
      </section>
      <section className="panel detail">
        {!post ? <div className="empty">게시글을 선택하세요.</div> : <>
          <div className="title"><div><h1>{post.title}</h1><small>{post.user} · 조회 {post.viewCount}</small></div>
            <div className="row">{post.canEdit && <button onClick={editPost}>수정</button>}{post.canDelete && <button className="danger" onClick={removePost}>삭제</button>}</div></div>
          <p className="body">{post.body}</p>
          {!!post.images?.length && <div className="images">{post.images.map((image) => <img key={image.url} src={image.url} alt={image.originalName || "첨부 이미지"} />)}</div>}
          <ReactionButtons target="post" id={post.id} reaction={post} onChanged={(next) => setPost({ ...post, like: next.likeCount, dislike: next.dislikeCount, myReaction: next.myReaction })} onError={setNotice} />
          <hr />
          <h2>댓글</h2>
          {user && <form className="comment-form" onSubmit={createComment}><textarea name="content" maxLength="1000" required /><button>댓글 등록</button></form>}
          {comments.items.map((comment) => <CommentItem key={comment.id} comment={comment} postId={post.id} onReload={() => loadComments(comments.page)} onError={setNotice} />)}
          <Pager value={comments} onChange={loadComments} />
        </>}
      </section>
    </main>
  </>;
}

function formatDate(value) {
  if (!value) return "";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString("ko-KR");
}

export default App;
