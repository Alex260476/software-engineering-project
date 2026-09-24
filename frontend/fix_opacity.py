import os

index_css = '''@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --color-background: 255 255 255;
    --color-surface: 243 244 246;
    --color-primary: 59 130 246;
    --color-text: 17 24 39;
    --color-muted: 107 114 128;
    --color-border: rgba(0, 0, 0, 0.1);
  }

  .dark {
    --color-background: 10 10 10;
    --color-surface: 26 26 26;
    --color-primary: 59 130 246;
    --color-text: 255 255 255;
    --color-muted: 163 163 163;
    --color-border: rgba(255, 255, 255, 0.1);
  }
}

@layer utilities {
  .glass {
    @apply bg-surface/80 backdrop-blur-md border border-[var(--color-border)];
  }
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  @apply bg-background text-text transition-colors duration-300;
}
'''
with open('src/index.css', 'w') as f: f.write(index_css)

tailwind_config = '''/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: 'rgb(var(--color-background) / <alpha-value>)',
        surface: 'rgb(var(--color-surface) / <alpha-value>)',
        primary: 'rgb(var(--color-primary) / <alpha-value>)',
        text: 'rgb(var(--color-text) / <alpha-value>)',
        muted: 'rgb(var(--color-muted) / <alpha-value>)',
        border: 'var(--color-border)'
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
'''
with open('tailwind.config.js', 'w') as f: f.write(tailwind_config)
