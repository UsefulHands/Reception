export const environment = {
  production: true,
  apiUrl: 'http://app:8080/api/v1'
};

// ng serve development için çalışır, build yaptıktan sonra kullanmazsın.
// Geliştirme sırasında → ng serve (live reload, dev server, environment.ts)
// Production / Docker → ng build --configuration=production → dist/ üretir → Nginx veya başka static server ile serve edilir
// Özet:
// ng serve → sadece development
// Docker veya production → build → serve dist/
