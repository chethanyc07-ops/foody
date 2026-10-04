import { Html, Head, Main, NextScript } from 'next/document';

export default function Document() {
  return (
    <Html lang="en">
      <Head>
        <meta name="description" content="EcoPack AI - Food Packaging Material Recommendation System" />
        <link rel="icon" href="/favicon.ico" />
      </Head>
      <body className="min-h-screen bg-slate-50 text-slate-900 antialiased">
        <Main />
        <NextScript />
      </body>
    </Html>
  );
}
