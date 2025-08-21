import os
from PIL import Image


images_dir = r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\images\val"   
labels_dir = r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\labels\val"  
def add_bbox(image_name, class_id):
    label_name = os.path.splitext(image_name)[0] + ".txt"
    label_path = os.path.join(labels_dir, label_name)
    yolo_line = f"{class_id} 0.5 0.5 1.0 1.0\n"
    with open(label_path, "a") as f: 
         f.write(yolo_line)

    print(f"✅ Added bbox to {label_path}")


for x in os.listdir(images_dir):
    if x.endswith('.jpg') or x.endswith('.png'):
        print(f"Processing {x}...")
        add_bbox(x, 0)  
def video_detection(model, video_path):
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        print("❌ Error: Could not open video.")
        return

    # Define output video writer
    fourcc = cv2.VideoWriter_fourcc(*"mp4v")  # codec
    out = cv2.VideoWriter(
        "output_video.mp4",
        fourcc,
        cap.get(cv2.CAP_PROP_FPS),
        (int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)))
    )

    while True:
        ret, frame = cap.read()
        if not ret:
            break

   
        results = model(frame, conf=0.1, imgsz=1280)

        annotated_frame = results[0].plot()

       
        cv2.imshow("Snack Detection - Video", annotated_frame)

       
        out.write(annotated_frame)

       
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    out.release()
    cv2.destroyAllWindows()