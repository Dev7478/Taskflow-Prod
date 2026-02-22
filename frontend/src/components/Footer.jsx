const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-blue-600 text-white py-4 mt-auto">
      <div className="max-w-7xl mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-2">
        <p className="text-sm">
          © {currentYear} <span className="font-semibold">TaskFlow V3</span>.
          All rights reserved.
        </p>

        <p className="text-sm opacity-90">
            Thanks for using
        </p>
      </div>
    </footer>
  );
};

export default Footer;
