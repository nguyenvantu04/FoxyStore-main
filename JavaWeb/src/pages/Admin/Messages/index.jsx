import React, { useState, useRef, useEffect } from "react";
import { Send, Check, User, Users, MessageCircle } from "lucide-react";

// Mock data
const mockUsers = [
  { id: 1, name: "Nguyễn Văn A", online: true },
  { id: 2, name: "Trần Thị B", online: false },
  { id: 3, name: "Lê Văn C", online: true },
];

const mockConversations = {
  1: [
    { from: "admin", text: "Chào bạn, tôi có thể giúp gì?", time: "09:00", read: true },
    { from: "user", text: "Tôi muốn hỏi về đơn hàng.", time: "09:01", read: true },
    { from: "admin", text: "Bạn cần hỗ trợ gì?", time: "09:02", read: false },
  ],
  2: [
    { from: "user", text: "Shop còn size M không?", time: "08:30", read: true },
    { from: "admin", text: "Dạ còn bạn nhé!", time: "08:31", read: true },
  ],
  3: [],
};

function MessagesAdmin() {
  const [users] = useState(mockUsers);
  const [selectedUser, setSelectedUser] = useState(users[0]);
  const [conversations, setConversations] = useState(mockConversations);
  const [input, setInput] = useState("");
  const chatEndRef = useRef(null);

  // Scroll to bottom when conversation changes
  useEffect(() => {
    if (chatEndRef.current) {
      chatEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [selectedUser, conversations]);

  // Đánh dấu đã đọc khi mở hội thoại
  useEffect(() => {
    setConversations(prev => {
      const conv = prev[selectedUser.id] || [];
      const updated = conv.map(msg =>
        msg.from === "user" ? { ...msg, read: true } : msg
      );
      return { ...prev, [selectedUser.id]: updated };
    });
  }, [selectedUser]);

  // Gửi tin nhắn
  const handleSend = (e) => {
    e.preventDefault();
    if (!input.trim()) return;
    setConversations(prev => ({
      ...prev,
      [selectedUser.id]: [
        ...(prev[selectedUser.id] || []),
        { from: "admin", text: input, time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }), read: false }
      ]
    }));
    setInput("");
  };

  return (
    <div className="w-full min-h-[calc(100vh-80px)] flex bg-gray-100">
      {/* Sidebar user list */}
      <aside className="w-72 bg-white border-r border-gray-200 flex flex-col">
        <div className="h-16 flex items-center px-6 border-b border-gray-100 font-bold text-indigo-700 text-lg gap-2">
          <Users size={22} /> Chat Admin
        </div>
        <div className="flex-1 overflow-y-auto">
          {users.map(user => {
            const unread = (conversations[user.id] || []).some(msg => msg.from === "user" && !msg.read);
            return (
              <button
                key={user.id}
                className={`w-full flex items-center gap-3 px-5 py-3 text-left hover:bg-indigo-50 transition
                  ${selectedUser.id === user.id ? "bg-indigo-100" : ""}`}
                onClick={() => setSelectedUser(user)}
              >
                <div className={`h-9 w-9 rounded-full flex items-center justify-center font-bold text-lg shadow ${user.online ? "bg-green-100 text-green-700" : "bg-gray-200 text-gray-500"}`}>
                  <User size={20} />
                </div>
                <div className="flex-1">
                  <div className="font-semibold">{user.name}</div>
                  <div className="text-xs text-gray-400">{user.online ? "Online" : "Offline"}</div>
                </div>
                {unread && (
                  <span className="w-2.5 h-2.5 rounded-full bg-red-500 animate-pulse"></span>
                )}
              </button>
            );
          })}
        </div>
      </aside>
      {/* Chat window */}
      <main className="flex-1 flex flex-col bg-gray-50">
        <header className="h-16 flex items-center px-8 border-b border-gray-200 bg-white gap-3">
          <MessageCircle size={22} className="text-indigo-600" />
          <div className="font-bold text-lg text-indigo-700">{selectedUser.name}</div>
          <span className={`ml-2 text-xs px-2 py-1 rounded-full ${selectedUser.online ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}>
            {selectedUser.online ? "Online" : "Offline"}
          </span>
        </header>
        <section className="flex-1 overflow-y-auto px-8 py-6">
          {(conversations[selectedUser.id] || []).map((msg, idx) => (
            <div key={idx} className={`flex mb-2 ${msg.from === "admin" ? "justify-end" : "justify-start"}`}>
              <div className={`max-w-lg px-4 py-2 rounded-lg shadow
                ${msg.from === "admin"
                  ? "bg-indigo-500 text-white rounded-br-2xl"
                  : "bg-white text-gray-800 border border-gray-200 rounded-bl-2xl"
                }`}>
                <div>{msg.text}</div>
                <div className="text-xs mt-1 flex items-center gap-1 justify-end">
                  <span>{msg.time}</span>
                  {msg.from === "admin" && (
                    <Check size={14} className={msg.read ? "text-green-400" : "text-gray-300"} />
                  )}
                </div>
              </div>
            </div>
          ))}
          <div ref={chatEndRef}></div>
        </section>
        <form onSubmit={handleSend} className="flex items-center gap-3 px-8 py-4 bg-white border-t border-gray-200">
          <input
            type="text"
            value={input}
            onChange={e => setInput(e.target.value)}
            placeholder="Nhập tin nhắn..."
            className="flex-1 border rounded-full px-4 py-2 focus:ring-2 focus:ring-indigo-400 bg-gray-50"
          />
          <button
            type="submit"
            className="bg-indigo-500 hover:bg-indigo-600 text-white rounded-full p-3 transition"
            title="Gửi"
          >
            <Send size={20} />
          </button>
        </form>
      </main>
    </div>
  );
}

export default MessagesAdmin;
