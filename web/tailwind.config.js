/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#BE1B39',
          light:   '#D63555',
          dark:    '#9C1530',
          50:      '#FDF2F4',
          100:     '#FADADF',
          200:     '#F5A3B0',
        },
        gold: {
          DEFAULT: '#F4C430',
          light:   '#F8D558',
          dark:    '#D4A420',
          50:      '#FFFBEB',
          100:     '#FEF3C7',
        },
        coral: {
          DEFAULT: '#E06060',
          light:   '#E88080',
          dark:    '#C94545',
          50:      '#FDF2F2',
        },
        sidebar: '#120709',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      backgroundImage: {
        'brand-gradient': 'linear-gradient(135deg, #BE1B39 0%, #8C1229 50%, #120709 100%)',
      },
    },
  },
  plugins: [],
}
