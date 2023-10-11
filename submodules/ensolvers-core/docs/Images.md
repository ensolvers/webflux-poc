# Images in AWS

Core supports upload/retrieval of images to/from AWS S3, also storing their metainformation in the DB through the 
[`Image`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/Image.java) class. 

The functionality is supported through [`ImageService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/ImageService.java) 
and [`ImageController`](../modules/ensolvers-core-backend-api/src/main/java/com/ensolvers/core/api/controller/ImageController.java)

## ImageController methods

[`ImageController`](../modules/ensolvers-core-backend-api/src/main/java/com/ensolvers/core/api/controller/ImageController.java) provides two methods 
that allow new images to be uploaded, returning the `Image` object created:

- `/upload` 
- `/upload-limit`

(details can be found in the Javadoc of the class directly)

## ImageService methods

On the other hand, [`ImageService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/ImageService.java) provides the following method
to deal with images

- `saveImage`
- `saveImageWithSizeLimit`
- `upload`
- `getImageURL`

(details can be found in the Javadoc of the class directly)

## Configuration

For enabling this service, the property `aws.image.bucket` needs to be provided, for instance

```
aws.images.bucket=${AWS_IMAGES_BUCKET:fake-bucket}
``` 

## Frontend image compression:

A good practice is to compress images before sending to backend, which saves computing power in the backend and also reduces
transfer sizes

There is a [ImageService](https://github.com/ensolvers/fox-typescript/blob/master/services/ImageService.ts) in fox-typescript repo that can be used for this. 

Sample below

```javascript
async uploadCompressImage(
    file: File,
    fileName: string,
    imageDTO: any,
    imageResizingProperties: ImageResizingProperties,
    size?: number,
): Promise<ApiResponse<Image>> {
    const resizedFileURI = await ImageUtils.resizeFile(file, imageResizingProperties) as string;
    const resizedFile = ImageUtils.createFileUsingBase64(resizedFileURI);
        
    const formData = new FormData();
    const fileExtension: string =
      resizedFile instanceof File ? this.obtainExtension(resizedFile) : "";
    formData.append("file", resizedFile, `${fileName}${fileExtension}`);

    return this.uploadImageToBackend(formData, imageDTO, size);
}
```

