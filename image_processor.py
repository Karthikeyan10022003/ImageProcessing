import cv2
from ultralytics import YOLO

# Load your trained model
model = YOLO("runs/detect/train/weights/best.pt")

# Path to your image
image_path = r"D:\img_processing_trial\WhatsApp Image 2025-07-10 at 10.21.07.jpeg"

# Read the image
frame = cv2.imread(image_path)

# Run detection
results = model(frame,conf=0.1,imgsz=1280)

# Annotate the image
annotated_frame = results[0].plot()

# Show the result
cv2.imshow("Snack Detection", annotated_frame)
cv2.waitKey(0)
cv2.destroyAllWindows()

# Save output image
cv2.imwrite("detected_image.jpg", annotated_frame)
