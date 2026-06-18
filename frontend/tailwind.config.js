/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'warm-orange': '#E07B4C',
        'warm-cream': '#FFF8F0',
        'warm-caramel': '#8B6B5A',
        'warm-yellow': '#FFECD2',
        'warm-peach': '#FFD4B8',
        'warm-pink': '#F5C6C6',
      }
    },
  },
  plugins: [],
}
