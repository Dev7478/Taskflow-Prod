import { useEffect, useState, useContext, useCallback } from "react";
import { useParams, Link } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import Scrollable from "../components/Scrollable";

const TicketComment = () => {
  const { token, user } = useContext(AuthContext);
  const { id } = useParams();

  const [ticket, setTicket] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editContent, setEditContent] = useState("");
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingComments, setLoadingComments] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);

    const fetchTicket = async () => {
      try {
        const res = await fetch(
          `http://localhost:8082/api/tickets/${encodeURIComponent(id)}`,
          {
            headers: token ? { Authorization: `Bearer ${token}` } : undefined,
          }
        );

        // protect against non-JSON or empty responses
        const ct = res.headers.get("content-type") || "";
        const data = ct.includes("application/json")
          ? await res.json().catch(() => null)
          : null;

        // backend may wrap payload in { status|statusCode, data }
        const payload = data && (data.data || data) ? data.data || data : data;

        if (res.ok && payload) {
          setTicket(payload);
        } else if (res.ok && !payload) {
          // maybe API returned ticket directly as JSON
          setTicket(data);
        } else {
          setError(
            data?.message || `Unable to load ticket (HTTP ${res.status})`
          );
        }
      } catch (err) {
        console.error("Fetch ticket error:", err);
        setError("Network error while loading ticket.");
      } finally {
        setLoading(false);
      }
    };

    fetchTicket();
  }, [id, token]);

  const fetchComments = useCallback(async () => {
    if (!id) return;
    setLoadingComments(true);

    try {
      const res = await fetch(
        `http://localhost:8082/api/tickets/${encodeURIComponent(id)}/comments`,
        {
          headers: token ? { Authorization: `Bearer ${token}` } : undefined,
        }
      );

      const ct = res.headers.get("content-type") || "";
      const data = ct.includes("application/json")
        ? await res.json().catch(() => null)
        : null;
      const payload = data && (data.data || data) ? data.data || data : data;

      if (res.ok && Array.isArray(payload)) {
        const normalized = payload.map((c) => ({
          id: c.id,
          text: c.comment ?? "",
          createdAt: c.createdAt ?? c.createdAt,
          updatedAt: c.updatedAt,
          authorEmail: c.authorEmail ?? c.author,
        }));
        setComments(normalized);
      } else {
        setComments([]);
      }
    } catch (err) {
      console.error("Fetch comments error:", err);
      setComments([]);
    } finally {
      setLoadingComments(false);
    }
  }, [id, token]);

  useEffect(() => {
    if (!id) return;
    fetchComments();
  }, [id, token, fetchComments]);

  const handleSubmitComment = async (e) => {
    if (e && e.preventDefault) e.preventDefault();
    if (!newComment || !newComment.trim()) return;
    if (!token) return;

    setSubmitting(true);
    try {
      const payload = { comment: newComment.trim() };

      const res = await fetch(
        `http://localhost:8082/api/tickets/${encodeURIComponent(id)}/comments`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );

      if (res.status === 201) {
        setNewComment("");
        await fetchComments();
      } else {
        const ct = res.headers.get("content-type") || "";
        const data = ct.includes("application/json")
          ? await res.json().catch(() => null)
          : null;
        console.error("Failed to post comment:", res.status, data);
      }
    } catch (err) {
      console.error("Submit comment error:", err);
    } finally {
      setSubmitting(false);
    }
  };

  const isAdminUser = () => {
    if (!user) return false;
    if (Array.isArray(user.roles) && user.roles.includes("ROLE_ADMIN"))
      return true;
    if (
      Array.isArray(user.authorities) &&
      user.authorities.includes("ROLE_ADMIN")
    )
      return true;
    return false;
  };

  const handleDeleteComment = async (commentId) => {
    if (!commentId) return;
    if (!token) return;

    const ok = window.confirm("Are you sure you want to delete this comment?");
    if (!ok) return;

    try {
      const res = await fetch(
        `http://localhost:8082/api/comments/${encodeURIComponent(commentId)}`,
        {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (res.ok) {
        await fetchComments();
      } else {
        const ct = res.headers.get("content-type") || "";
        const data = ct.includes("application/json")
          ? await res.json().catch(() => null)
          : null;
        console.error("Failed to delete comment:", res.status, data);
      }
    } catch (err) {
      console.error("Delete comment error:", err);
    }
  };

  const handleUpdateComment = async (commentId) => {
    if (!commentId) return;
    if (!editContent || !editContent.trim()) return;
    if (!token) return;

    setEditSubmitting(true);
    try {
      const payload = { comment: editContent.trim() };

      const res = await fetch(
        `http://localhost:8082/api/comments/${encodeURIComponent(commentId)}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );

      if (res.ok) {
        setEditingCommentId(null);
        setEditContent("");
        await fetchComments();
      } else {
        const ct = res.headers.get("content-type") || "";
        const data = ct.includes("application/json")
          ? await res.json().catch(() => null)
          : null;
        console.error("Failed to update comment:", res.status, data);
      }
    } catch (err) {
      console.error("Update comment error:", err);
    } finally {
      setEditSubmitting(false);
    }
  };

  return (
    <>
      <div className="p-6 max-w-4xl mx-auto">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold">Ticket Details</h2>
        </div>
        {loading ? (
          <div className="text-gray-600">Loading ticket...</div>
        ) : error ? (
          <div className="text-red-600">{error}</div>
        ) : ticket ? (
          <div className="bg-white p-4 rounded shadow">
            <h3 className="text-lg font-medium mb-1">{ticket.subject}</h3>
            <div className="text-sm text-gray-500 mb-2">
              Status: {ticket.status || "N/A"}
            </div>
            <p className="text-gray-800 mb-4">{ticket.description || "-"}</p>

            <div className="grid grid-cols-3 gap-4 text-sm text-gray-600 mb-4">
              <div>
                Start:{" "}
                {ticket.startDate
                  ? new Date(ticket.startDate).toLocaleDateString()
                  : "-"}
              </div>
              <div>
                Due:{" "}
                {ticket.dueDate
                  ? new Date(ticket.dueDate).toLocaleDateString()
                  : "-"}
              </div>
              <div>
                Updated:{" "}
                {ticket.updatedAt
                  ? new Date(ticket.updatedAt).toLocaleString()
                  : "-"}
              </div>
            </div>

            <hr className="my-4" />

            <h4 className="font-semibold mb-2">Comments</h4>
            {loadingComments ? (
              <div className="text-gray-600">Loading comments...</div>
            ) : comments.length === 0 ? (
              <div className="text-gray-500">No comments yet.</div>
            ) : (
              <ul className="space-y-3">
                {comments.map((c) => (
                  <li key={c.id} className="p-3 bg-gray-50 rounded">
                    {editingCommentId === c.id ? (
                      <div>
                        <textarea
                          value={editContent}
                          onChange={(e) => setEditContent(e.target.value)}
                          className="w-full border border-gray-300 rounded p-2 resize-y text-sm"
                          rows={3}
                        />
                        <div className="mt-2 flex justify-end gap-2">
                          <button
                            type="button"
                            onClick={() => handleUpdateComment(c.id)}
                            disabled={editSubmitting || !editContent.trim()}
                            className="px-3 py-1 bg-blue-600 text-white rounded text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            {editSubmitting ? "Saving..." : "Save"}
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setEditingCommentId(null);
                              setEditContent("");
                            }}
                            className="px-3 py-1 text-sm text-gray-600"
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    ) : (
                      <>
                        <div className="text-sm text-gray-700 whitespace-pre-wrap">{c.text}</div>
                        <div className="flex items-center justify-between mt-1">
                          <div className="text-xs text-gray-500">
                            {c.authorEmail || "Unknown"} •{" "}
                            {c.createdAt
                              ? new Date(c.createdAt).toLocaleString()
                              : "-"}
                          </div>
                          <div className="text-xs">
                            {(user?.email === c.authorEmail ||
                              isAdminUser()) && (
                              <>
                                <button
                                  type="button"
                                  onClick={() => {
                                    setEditingCommentId(c.id);
                                    setEditContent(c.text || "");
                                  }}
                                  className="text-sm text-blue-600 hover:underline mr-3"
                                >
                                  Edit
                                </button>
                                <button
                                  type="button"
                                  onClick={() => handleDeleteComment(c.id)}
                                  className="text-sm text-red-600 hover:underline"
                                >
                                  Delete
                                </button>
                              </>
                            )}
                          </div>
                        </div>
                      </>
                    )}
                  </li>
                ))}
              </ul>
            )}
            <hr className="my-4" />

            <form onSubmit={handleSubmitComment} className="mt-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Add a comment
              </label>
              <textarea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                className="w-full border border-gray-300 rounded p-2 resize-y min-h-[80px] text-sm"
                placeholder="Write your comment..."
                rows={4}
              />

              <div className="mt-3 flex justify-end">
                <button
                  type="submit"
                  disabled={!newComment.trim() || submitting}
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {submitting ? "Posting..." : "Post Comment"}
                </button>
              </div>
            </form>
          </div>
        ) : (
          <div className="text-gray-600">Ticket not found.</div>
        )}
        <Scrollable />
      </div>
    </>
  );
};

export default TicketComment;
