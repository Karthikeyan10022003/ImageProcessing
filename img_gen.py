import os
from PIL import Image


input_dir = r"D:\img_processing_trial\img_processing_trial\images\KitKat_chocolate"
output_dir = "rotated_images"

os.makedirs(output_dir, exist_ok=True)

for filename in os.listdir(input_dir):
    if filename.lower().endswith(('.jpg', '.jpeg', '.png')): 
        img_path = os.path.join(input_dir, filename)

     
        pil_img = Image.open(img_path)

        
        for angle in range(360):
            rotated = pil_img.rotate(angle, expand=True)

           
            output_path = os.path.join(output_dir, f"{os.path.splitext(filename)[0]}_rotated_{angle}.png")
            rotated.save(output_path)

print(f"Generated rotated images for all files in '{input_dir}'")
