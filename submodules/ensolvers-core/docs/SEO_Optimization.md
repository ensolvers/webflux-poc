# SEO Optimization

Core provides ways to manage SEO Optimization when a website/frontend is seved statically. Below there
is a detailed explanation of the problem and how Core can help to solve, yet preserving the site served statically

## The Problem

Let's assume we have  a SPA implemented in React, which contains two paths: `/` and `/account` in which a user
can see the index of the app and its account respectively.

When the app is built, the bundler generates and `index.html` file that loads all the JS and CSS bundles thar 
represent the app itself. So, when the app is published in a CDN, when we try to access `/home` or `/account`, the response
would be naturally a 404 since those files do not exist (`index.html` is the only valid page). A simple way
for solving this is configuring the CDN for returning `index.html` as a default "error page" for the 404 error. IN
this case, what is going to happen is the following:
1. The user requests `/account`
2. A 404 error is triggered in the CDN since a resource named `account` does not exist. That error is captured by the CDN configuration 
   and a `200 OK` response is returned with the content of `index.html` as a body
3. The app is loaded in the browser, it detects that the current path is `/account`, the client-side framework (in this case
   react-navigator probably) detects the route and renders the page

Now, for SEO purposes, we need some static HTML content to be tuned to be captured by web crawlers. This now becomes
a problem since we return the  same HTML content (`index.html`) for all paths.

## The solution

A quick solution is to generate a special version of the `index.html` file for each path with the custom tags. For 
instance, in our case, the final version will have

```
index.html
home
account
```

As 3 different resources, each one with the custom proper HTML tags (mostly `title` and `meta` ones)

This introduces a second problem: when a new version of the app is deployed and something changes on the `index.html`, 
we need to update not only `index.html` but the other two. 

## The final solution

To avoid having to update all the pages (that can be hundreds or thousands), Core opts to pre-process the `index.html`
file to obtain a simplified version in which only the `title` and `meta` tags that change from page to page. The resulting
page would be something like

```html
<!doctype html>
<html lang="en">
 <head>
  <!-- Block 1: meta tags -->   
  <title>Title</title>
  <meta name="description" content="Description">
  <meta name="og:description" content="Description">
  <meta name="og:title" content="Title">
  <meta name="og:site_name" content="Site Name">
  <meta name="og:url" content="https://qa-web.ticketon.com/event/ceccompany-event-buenos-aires-buenos-aires-2022-10-27-xe8dnufpvfrc">
  <meta name="og:image" content="https://d78npzulg9d2i.cloudfront.net/cropped-66ddd949-c232-4b5b-9b12-951b34d12470.jpeg">
  <meta name="og:type" content="website">

   <!-- Block 2: generic functions (initfns.js) and dynamic imports via those
                 functions (init.js) -->   
  <script src="/scripts/initfns.js"></script>
  <script src="/scripts/init.js"></script> <!-- generated-at: 2022.12.28 07:13:21 EST -->
 </head>
 <body>
  <noscript>
   You need to enable JavaScript to run this app.
  </noscript>
  <div id="root"></div>
  <!-- Block 3: originally embedded JS in index.html that is moved to external files -->
  <script src="/scripts/script0.js"></script>
 </body>
</html>
```

So, basically all pages (`index.html`, `account` and `home`) will share exactly the same structure, the only thing that 
will change is the `title` and `meta` tags.

If a new version of the app is deployed, we just need to re-run the post-processing function that generates the `init.js`
and `script0.js` files from the new source code.

## How to implement this

Let's use a simple example to show how this way of SEO Optimization is implemented

By calling `createOptimizedVersion`, the code below will do the following:
- Load the contents of `https://my-prod-site.com/index.html`
- Generates a set of files as a result (a simplified HTML version called `account`, updated `init.js` and `script0.js`)
- Writes all this files into the bucket `cdn.s3.bucket`
- Triggers an invalidation to the CloudFront `XYZ` distribution, which must be linked to the `cdn.s3.bucket` as a primary
  content source

Result: `/account` will be accessible in a couple of seconds, implementing a simplified version of the HTML and
correct SEO information

```java
public class SEOOptimizationTest {

    static {
       CloudfrontSEOOptimizationService service = new CloudfrontSEOOptimizationService("https://my-prod-site.com/index.html", "cdn.s3.bucket",
               "XYZ");

       service.createOptimizedVersion("account",
               SEOMetadata.builder().title("Title").siteName("Site Name").description("Description").build());
    }
    
  }
  
```

