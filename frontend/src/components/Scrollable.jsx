import { useEffect, useState } from "react";

const Scrollable = () => {
  const [showScrollTop, setShowScrollTop] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      const shouldShow = window.scrollY > 200;
      setShowScrollTop((prev) => (prev !== shouldShow ? shouldShow : prev));
    };

    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  return (
    <>
      {showScrollTop && (
        <button
          onClick={scrollToTop}
          className="
            fixed bottom-6 right-6 z-50
            w-12 h-12
            flex items-center justify-center
            rounded-full
            bg-blue-600 text-white
            shadow-lg
            hover:bg-blue-700
            transition-all duration-300 ease-in-out
            animate-fade-in font-bold text-3xl
          "
          aria-label="Scroll to top"
          title="Back to top"
        >
          ↑
        </button>
      )}
    </>
  );
};

export default Scrollable;
