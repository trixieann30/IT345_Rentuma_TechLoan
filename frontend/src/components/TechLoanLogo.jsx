export default function TechLoanLogo({ size = 'md', showSubtitle = true }) {
  const sizes = {
    sm: { icon: 'text-xl', title: 'text-xl', subtitle: 'text-xs' },
    md: { icon: 'text-3xl', title: 'text-3xl', subtitle: 'text-sm' },
    lg: { icon: 'text-5xl', title: 'text-5xl', subtitle: 'text-base' },
  }
  
  return (
    <div className="flex flex-col items-center gap-2">
      <div className="flex items-center gap-2">
        <div className="w-10 h-10 bg-gradient-to-br from-primary to-primary-light rounded-lg flex items-center justify-center transform hover:scale-110 transition-transform">
          <span className={`${sizes[size].icon} text-white`}>⚙️</span>
        </div>
        <h1 className={`${sizes[size].title} font-bold bg-gradient-to-r from-primary to-primary-light bg-clip-text text-transparent`}>
          TechLoan
        </h1>
      </div>
      {showSubtitle && (
        <p className={`${sizes[size].subtitle} text-gray-600 font-medium`}>
          Lab Equipment Borrowing System
        </p>
      )}
    </div>
  )
}
