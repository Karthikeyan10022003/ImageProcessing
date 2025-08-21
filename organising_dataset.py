import os
import shutil
import random
from pathlib import Path


source_dir = Path(r"D:\img_processing_trial\img_processing_trial\test_images")  # Your current images folder
output_dir = Path(r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat")  # YOLOv8 dataset output folder
train_ratio = 0.8  # Train/val split ratio


for split in ["train", "val"]:
    (output_dir / "images" / split).mkdir(parents=True, exist_ok=True)
    (output_dir / "labels" / split).mkdir(parents=True, exist_ok=True)


classes = sorted([d.name for d in source_dir.iterdir() if d.is_dir()])
class_map = {cls: i for i, cls in enumerate(classes)}

print("Classes found:", class_map)


for cls_name, cls_id in class_map.items():
    cls_folder = source_dir / cls_name
    image_files = [f for f in cls_folder.glob("*.*") if f.suffix.lower() in [".jpg", ".jpeg", ".png"]]
    random.shuffle(image_files)
    
    split_index = int(len(image_files) * train_ratio)
    train_files = image_files[:split_index]
    val_files = image_files[split_index:]

    for split, files in zip(["train", "val"], [train_files, val_files]):
        for img_path in files:
            
            dest_img_path = output_dir / "images" / split / img_path.name
            shutil.copy(img_path, dest_img_path)

      
            label_path = output_dir / "labels" / split / f"{img_path.stem}.txt"
            with open(label_path, "w") as f:
                pass


yaml_path = output_dir / "data.yaml"
with open(yaml_path, "w") as f:
    f.write(f"path: {output_dir}\n")
    f.write("train: images/train\n")
    f.write("val: images/val\n\n")
    f.write("names:\n")
    for i, cls in enumerate(classes):
        f.write(f"  {i}: {cls}\n")

print(f"✅ YOLOv8 dataset prepared at {output_dir}")
print(f"📄 data.yaml created at {yaml_path}")