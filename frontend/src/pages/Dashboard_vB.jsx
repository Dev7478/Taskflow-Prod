import { useEffect, useState, useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import ConfirmationModal from "../components/ConfirmationModal";

export default function Dashboard() {
  const { token, user } = useContext(AuthContext);
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);

  // modal / form state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [subject, setSubject] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // notification { type: 'success'|'error', message: string }
  const [notification, setNotification] = useState(null);

  const [updatingStatus, setUpdatingStatus] = useState(null);

  // confirmation modal state
  const [confirmModal, setConfirmModal] = useState({
    isOpen: false,
    ticketId: null,
    subject: null,
    isDeleting: false,
  });

  const TICKET_STATUSES = ["NEW", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED", "PENDING"];

  const navigate = useNavigate();

  const fetchTickets = async () => {
    if (!token || !user?.email) return;
    setLoading(true);
    try {
      const res = await fetch(
        `http://localhost:8082/api/tickets/user/${encodeURIComponent(user.email)}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const data = await res.json();
      if (res.ok && data.status === 200) {
        setTickets(data.data || []);
      } else {
        setTickets([]);
        console.error("Failed to load tickets:", data);
      }
    } catch (err) {
      console.error("Error fetching tickets:", err);
      setTickets([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token || !user?.email) return;
    fetchTickets();
  }, [token, user?.email]); // eslint-disable-line

  const openModal = () => {
    setSubject("");
    setDescription("");
    setDueDate("");
    setIsModalOpen(true);
  };

  const closeModal = () => {
    if (submitting) return;
    setIsModalOpen(false);
  };

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 4000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!subject.trim() || !description.trim() || !dueDate) {
      showNotification("error", "Please fill all fields.");
      return;
    }
    if (!token || !user?.email) {
      showNotification("error", "Authentication required.");
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        subject: subject.trim(),
        description: description.trim(),
        dueDate,
      };

      const res = await fetch(
        `http://localhost:8082/api/tickets/user/${encodeURIComponent(user.email)}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );

      const data = await res.json();
      if (res.ok && (data.status === 200 || data.status === 201)) {
        showNotification("success", data.message || "Ticket created successfully.");
        closeModal();
        await fetchTickets(); // refresh table
      } else {
        const msg = data?.message || `Failed to create ticket (HTTP ${res.status})`;
        showNotification("error", msg);
      }
    } catch (err) {
      console.error("Submit error:", err);
      showNotification("error", "Network error while creating ticket.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleStatusUpdate = async (ticketId, newStatus) => {
    if (!token) {
      showNotification("error", "Authentication required");
      return;
    }

    if (!TICKET_STATUSES.includes(newStatus)) {
      showNotification("error", "Invalid status selected");
      return;
    }

    setUpdatingStatus(ticketId);
    try {
      const res = await fetch(`http://localhost:8082/api/tickets/status`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          ticketId: ticketId,
          status: newStatus
        })
      });

      const data = await res.json();
      if (res.ok && data.status === 200) {
        showNotification("success", "Status updated successfully");
        await fetchTickets(); // refresh tickets
      } else {
        const msg = data?.message || `Failed to update status (${res.status})`;
        showNotification("error", msg);
      }
    } catch (err) {
      console.error("Status update error:", err);
      showNotification("error", "Network error while updating status");
    } finally {
      setUpdatingStatus(null);
    }
  };

  const handleDeleteClick = (ticketId, ticketSubject) => {
    setConfirmModal({
      isOpen: true,
      ticketId,
      subject: ticketSubject,
      isDeleting: false,
    });
  };

  const handleConfirmDelete = async () => {
    const { ticketId } = confirmModal;
    if (!token || !ticketId) {
      showNotification("error", "Authentication required");
      return;
    }

    setConfirmModal((prev) => ({ ...prev, isDeleting: true }));
    try {
      const res = await fetch(`http://localhost:8082/api/tickets/${ticketId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      const data = await res.json();
      if (res.ok && data.status === 200) {
        showNotification("success", data.message || "Ticket deleted successfully");
        setConfirmModal({ isOpen: false, ticketId: null, subject: null, isDeleting: false });
        await fetchTickets(); // refresh tickets
      } else {
        const msg = data?.message || `Failed to delete ticket (${res.status})`;
        showNotification("error", msg);
      }
    } catch (err) {
      console.error("Delete error:", err);
      showNotification("error", "Network error while deleting ticket");
    } finally {
      setConfirmModal((prev) => ({ ...prev, isDeleting: false }));
    }
  };

  const handleCancelDelete = () => {
    setConfirmModal({
      isOpen: false,
      ticketId: null,
      subject: null,
      isDeleting: false,
    });
  };

  return (
    <div className="flex flex-col p-8 min-h-screen bg-gray-50">
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Task Management Dashboard</h1>
          <p className="text-sm text-gray-600 mt-1">Manage and track your assigned tasks</p>
        </div>

        <div className="flex items-center gap-4">
          <button
            onClick={openModal}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium"
          >
            New Ticket
          </button>
        </div>
      </div>

      {notification && (
        <div
          className={`mb-4 p-3 rounded ${notification.type === "success" ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
            }`}
        >
          {notification.message}
        </div>
      )}

      <div className="bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
          <h2 className="text-lg font-semibold text-gray-800">Assigned Tickets</h2>
        </div>

        {loading ? (
          <div className="p-8 text-center text-gray-500">Loading tickets...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Subject</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Start Date</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Due Date</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {tickets.map((ticket) => (
                  <tr key={ticket.id} className="hover:bg-gray-50 cursor-pointer transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ticket.id}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ticket.subject}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <select
                        value={ticket.status}
                        onChange={(e) => handleStatusUpdate(ticket.id, e.target.value)}
                        disabled={updatingStatus === ticket.id}
                        className={`px-2 py-1 text-sm rounded border ${updatingStatus === ticket.id
                          ? "opacity-50 cursor-not-allowed"
                          : "cursor-pointer hover:border-blue-500"
                          } ${ticket.status === "NEW" ? "bg-green-100 text-green-800" :
                            ticket.status === "IN_PROGRESS" ? "bg-blue-100 text-blue-800" :
                              ticket.status === "RESOLVED" ? "bg-purple-100 text-purple-800" :
                                ticket.status === "CLOSED" ? "bg-gray-100 text-gray-800" :
                                  ticket.status === "PENDING" ? "bg-yellow-100 text-yellow-800" :
                                    "bg-gray-50 text-gray-800"
                          }`}
                      >
                        {TICKET_STATUSES.map((status) => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {ticket.startDate ? new Date(ticket.startDate).toLocaleDateString() : "-"}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {ticket.dueDate ? new Date(ticket.dueDate).toLocaleDateString() : "-"}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {user?.role === "ROLE_ADMIN" && (
                        <button
                          onClick={() => handleDeleteClick(ticket.id, ticket.subject)}
                          className="text-red-600 hover:text-red-900 font-medium"
                          title="Delete ticket"
                        >
                          Delete
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {tickets.length === 0 && !loading && (
              <div className="text-center py-8 text-gray-500">No tickets available at this time.</div>
            )}
          </div>
        )}
      </div>

      {/* Confirmation Modal for Delete */}
      <ConfirmationModal
        isOpen={confirmModal.isOpen}
        title="Delete Ticket"
        message={`Are you sure you want to delete the ticket "${confirmModal.subject}"? This action cannot be undone.`}
        confirmText="Delete"
        cancelText="Cancel"
        confirmButtonClass="bg-red-600 hover:bg-red-700"
        onConfirm={handleConfirmDelete}
        onCancel={handleCancelDelete}
        isLoading={confirmModal.isDeleting}
      />

      {/* Modal for New Ticket */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black opacity-40" onClick={closeModal} />

          <div className="bg-white rounded-lg shadow-lg z-10 w-full max-w-lg mx-4">
            <form onSubmit={handleSubmit}>
              <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
                <h3 className="text-lg font-semibold text-gray-900">New Ticket</h3>
                <button
                  type="button"
                  onClick={closeModal}
                  className="text-gray-500 hover:text-gray-700"
                  aria-label="Close"
                >
                  ✕
                </button>
              </div>

              <div className="p-6 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Subject</label>
                  <input
                    type="text"
                    value={subject}
                    onChange={(e) => setSubject(e.target.value)}
                    className="mt-1 block w-full border rounded px-3 py-2"
                    required
                    placeholder="Brief subject"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">Description</label>
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="mt-1 block w-full border rounded px-3 py-2"
                    rows={4}
                    required
                    placeholder="Describe the issue"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">Due date</label>
                  <input
                    type="date"
                    value={dueDate}
                    onChange={(e) => setDueDate(e.target.value)}
                    className="mt-1 block w-full border rounded px-3 py-2"
                    required
                  />
                </div>
              </div>

              <div className="px-6 py-4 border-t border-gray-200 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={closeModal}
                  disabled={submitting}
                  className="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-60"
                >
                  {submitting ? "Submitting..." : "Submit"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}