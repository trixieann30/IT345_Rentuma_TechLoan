export default function TechLoanLogo({ size = 'md' }) {
  const sizes = {
    sm: 'text-2xl',
    md: 'text-4xl',
    lg: 'text-5xl',
  }
  return (
    <div className="text-center">
      <h1 className={`${sizes[size]} font-bold text-primary tracking-tight`}>
        TechLoan
      </h1>
      <p className="text-gray-500 text-sm mt-1">
        CIT-U Lab Equipment Borrowing System
      </p>
    </div>
  )
}
