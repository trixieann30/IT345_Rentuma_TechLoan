/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#8B0000',
          light:   '#A52A2A',
          dark:    '#5C0000',
          50:      '#FAF5F5',
          100:     '#F3EBEB',
          200:     '#E74C3C',
        },
        accent: {
          DEFAULT: '#27AE60',
          light:   '#2ECC71',
        },
        warning: {
          DEFAULT: '#F39C12',
          light:   '#F1C40F',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      spacing: {
        safe: 'max(1rem, env(safe-area-inset-bottom))',
      },
      backdropBlur: {
        xs: '2px',
      }
    },
  },
  plugins: [],
}
