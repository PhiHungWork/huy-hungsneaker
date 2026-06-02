const fs = require('fs');

const files = [
    'src/main/resources/templates/home.html',
    'src/main/resources/templates/public-products.html',
    'src/main/resources/templates/public-product-detail.html',
    'src/main/resources/templates/public-brand.html'
];

files.forEach(filepath => {
    if (fs.existsSync(filepath)) {
        let content = fs.readFileSync(filepath, 'utf8');
        
        // Update the mega menu links
        content = content.replace(/href="#nike"/g, 'href="/products/brand/nike"');
        content = content.replace(/href="#adidas"/g, 'href="/products/brand/adidas"');
        content = content.replace(/href="#jordan"/g, 'href="/products/brand/jordan"');
        
        // Remove the 'dd-active' class from all of them since it's a real link now
        // Wait, on the mega menu, 'dd-active' was just for styling. It's okay to leave it, or remove it.
        // I will leave it, it might have hover styles.
        
        fs.writeFileSync(filepath, content, 'utf8');
    }
});
console.log("Updated mega menu links in all templates");
