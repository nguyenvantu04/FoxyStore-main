import React, { useState, useRef, useEffect } from "react";
import { MessageCircle } from "lucide-react";
import { request } from "../../untils/request"; 

const ChatBot = () => {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    { from: "bot", text: "Xin chào! Tôi có thể giúp gì cho bạn?" }
  ]);
  const [input, setInput] = useState("");
  const messagesEndRef = useRef(null);

  // Tự động cuộn xuống cuối khi có tin nhắn mới
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // Gửi câu hỏi đến API và nhận phản hồi thực tế
  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;
    const userMsg = { from: "user", text: input };
    setMessages((msgs) => [...msgs, userMsg]);
    setInput("");

    try {
      // Gửi request đến API
      const res = await request.post(
        "http://localhost:8080/api/v1/openai/ask",
        { question: input }
      );
      // Lấy câu trả lời từ API
      const answer =
        res?.data?.result?.answer ||
        "Xin lỗi, tôi không thể trả lời câu hỏi này vào lúc này.";
      setMessages((msgs) => [
        ...msgs,
        { from: "bot", text: answer }
      ]);
    } catch (err) {
      setMessages((msgs) => [
        ...msgs,
        { from: "bot", text: "Đã xảy ra lỗi khi kết nối tới máy chủ." }
      ]);
    }
  };

  return (
    <>
      {/* Icon mở chat */}
      <button
        className="fixed bottom-8 right-8 z-50 bg-white hover:bg-gray-100 text-black rounded-full p-3 shadow-lg focus:outline-none border border-gray-200 transition-all"
        onClick={() => setOpen((o) => !o)}
        aria-label="Mở chatbot"
      >
        <MessageCircle className="w-5 h-5" color="black" />
      </button>

      {/* Popup chat */}
      {open && (
        <div
          className="fixed bottom-10 right-8 z-50 w-[600px] max-w-[95vw] bg-white rounded-2xl shadow-xl flex flex-col border border-gray-200 animate-fade-in"
          style={{ minHeight: 600, maxHeight: "90vh" }}
        >
          <div className="flex items-center justify-between px-7 py-4 border-b bg-white rounded-t-2xl">
            <span className="text-gray-800 font-semibold text-lg tracking-wide">ChatBot Hỗ trợ</span>
            <button
              className="text-gray-500 hover:text-gray-800 text-2xl font-bold transition"
              onClick={() => setOpen(false)}
              aria-label="Đóng"
            >
              ×
            </button>
          </div>
          <div
            className="flex-1 overflow-y-auto px-7 py-5 space-y-4 bg-white"
            style={{ minHeight: 500, maxHeight: "calc(90vh - 100px)" }}
          >
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={`flex ${msg.from === "user" ? "justify-end" : "justify-start"}`}
              >
                <div
                  className={`px-5 py-3 rounded-xl text-base max-w-[85%] shadow whitespace-pre-line font-sans ${
                    msg.from === "user"
                      ? "bg-blue-50 text-gray-900 border border-blue-100 rounded-br-md"
                      : "bg-gray-50 text-gray-900 border border-gray-200 rounded-bl-md"
                  }`}
                  style={
                    msg.from === "bot"
                      ? {
                          whiteSpace: "pre-line",
                          wordBreak: "break-word",
                          lineHeight: 1.7,
                          fontSize: "1.05rem",
                          background: "#f8fafc",
                          padding: "18px 22px",
                        }
                      : {}
                  }
                >
                  {/* Nếu là bot, tự động tách dòng cho các gạch đầu dòng hoặc số thứ tự */}
                  {msg.from === "bot"
                    ? msg.text
                        .split("\n")
                        .map((line, i) => (
                          <span key={i}>
                            {line}
                            <br />
                          </span>
                        ))
                    : msg.text}
                </div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>
          <form onSubmit={handleSend} className="flex border-t px-4 py-4 bg-white rounded-b-2xl gap-2">
            <input
              type="text"
              className="flex-1 border border-gray-200 rounded-lg px-4 py-2 text-base text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-200 transition bg-white"
              placeholder="Nhập tin nhắn..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              autoFocus
            />
            <button
              type="submit"
              className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg text-base font-medium shadow transition"
            >
              Gửi
            </button>
          </form>
        </div>
      )}
    </>
  );
};

export default ChatBot;
