import os

folders = [
    r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\labels\train",
    r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\labels\val"
]

for folder_path in folders:
    for filename in os.listdir(folder_path):
        if filename.lower().endswith(".txt"):
            file_path = os.path.join(folder_path, filename)
            open(file_path, 'w').close()
            print(f"✅ Emptied: {file_path}")
