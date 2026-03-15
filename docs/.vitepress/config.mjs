import { defineConfig } from 'vitepress'

export default defineConfig({
  title: "JEffect",
  description: "A functional effect library for Java with deferred computation and error handling",
  srcDir: '.',
  outDir: '.vitepress/dist',
  themeConfig: {
    logo: '/logo.svg',
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/guide/getting-started' },
      { text: 'API', link: '/api/effects-factory' },
      { text: 'GitHub', link: 'https://github.com/fike110/jeffect' }
    ],
    sidebar: {
      '/guide/': [
        {
          text: 'Guide',
          items: [
            { text: 'Getting Started', link: '/guide/getting-started' },
            { text: 'Core Concepts', link: '/guide/core-concepts' },
            { text: 'Creating Effects', link: '/guide/creating-effects' },
            { text: 'Transforming', link: '/guide/transforming' },
            { text: 'Executing', link: '/guide/executing' },
            { text: 'Combining', link: '/guide/combining' },
            { text: 'Error Handling', link: '/guide/error-handling' },
            { text: 'Spring Boot', link: '/guide/spring-boot' }
          ]
        }
      ],
      '/api/': [
        {
          text: 'API Reference',
          items: [
            { text: 'Effects Factory', link: '/api/effects-factory' },
            { text: 'Effect Interface', link: '/api/effect-interface' },
            { text: 'Result Type', link: '/api/result-type' }
          ]
        }
      ]
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/fike110/jeffect' }
    ],
    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2024 JEffect'
    },
    search: {
      provider: 'local'
    }
  }
})
