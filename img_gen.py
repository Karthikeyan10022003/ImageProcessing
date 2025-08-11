from icrawler.builtin import GoogleImageCrawler

products = ["Lays chips", "Kurkure", "KitKat chocolate"]
for product in products:
    crawler = GoogleImageCrawler(storage={'root_dir': f'images/{product.replace(" ", "_")}'})
    crawler.crawl(keyword=product, max_num=100)
