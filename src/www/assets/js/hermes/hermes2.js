document.addEventListener("DOMContentLoaded", () => {
    const elementsWithHermesSrc = document.querySelectorAll('[hermes-src]');
    const rowCountHeader = "hermes-chunk-row-count";
    const colCountHeader = "hermes-chunk-col-count";
    const imgWidthHeader = "hermes-img-width";
    const imgHeightHeader = "hermes-img-height";
    const chunkLocationHeader = "hermes-chunk-src";
    const chunkNameHeader = "hermes-chunk-name";
    
    elementsWithHermesSrc.forEach(async element => {
        const resource = element.getAttribute('hermes-src');
        const dataType = getImageType(resource);
        console.log("dataType ", dataType)
        console.log("heigth: " + element.clientHeight);
        console.log("width: " + element.clientWidth);
        var canvas = document.createElement("canvas");
        
        let response = await fetch(resource + '?hermes=true&hermesStep=1')
        if (response.ok) {
            let headers = response.headers;
            let rowCount = headers.get(rowCountHeader);
            let colCount = headers.get(colCountHeader);
            let imgWidth = headers.get(imgWidthHeader);
            let imgHeight = headers.get(imgHeightHeader);
            let chunkSrc = headers.get(chunkLocationHeader);
            let chunkName = headers.get(chunkNameHeader);
            canvas.width = 1000;
            canvas.height = 1000;
            const sliceWidth = imgWidth / colCount;
            const sliceHeight = imgHeight / rowCount;
            let images = [];

            let img1 = new Image();
            let img2 = new Image();
            let context = canvas.getContext("2d");
            canvas.style = "border: 5px solid black";
            img1.src = "assets/img/bigImg/330GigaPixeleschunks/img_0_3.png";
            img2.src = "assets/img/bigImg/330GigaPixeleschunks/img_0_4.png";
            /*img1.onload = function() {
                let context = canvas.getContext("2d");
                context.fillStyle = "black"
                context.lineWidth = 2
                context.drawImage(img1, 0, 0, 200, 200);
            }
            img2.onload = function() {
                context.drawImage(img2, sliceWidth * 0.6, 0, 200, 200);
            }*/

            console.log("sliceWidth = ", sliceWidth, "sliceHeight = ", sliceHeight)
            for (let row = 0; row < rowCount; row++) {
                for (let col = 0; col < colCount; col++) {
                    console.log("row = ", row, "col = ", col)
                    let imagePath = `${chunkSrc}${chunkName}${row}_${col}.${dataType.ext}`;
                    let img = new Image(100, 100);
                    img.src = imagePath;
                    
                    let context = canvas.getContext("2d");

                    images.push({"image": img, "col": col, "row": row})
                    img.onload  = function() {
                        context.drawImage(img, col, row);
                    }
                    /*fetch(`${chunkSrc}${chunkName}${row}_${col}.${dataType.ext}?hermes=true&hermesStep=2`)
                        .then(response => {
                            if (response.ok) {
                                const reader = response.body.getReader();
                                let imageData = [];
                                return new ReadableStream({
                                    async start(controller) {
                                        try {
                                            var index = 0;
                                            while (true) {
                                                const { done, value } = await reader.read();
                                                if (done) {
                                                    imageData[0] = imageData[0].slice(1);
                                                    const blob = new Blob(imageData, { type: dataType.ext });
                                                    const imageUrl = URL.createObjectURL(blob);
                                                    /*element.onload = "this.style.display = 'block'"
                                                    element.src = imageUrl;
                                                    console.log(resource);
                                                    console.log(imageData);
                                                    console.log(imageUrl);
                                                    controller.close();
                                                    
                                                    renderImage(imageUrl, context, col, row, sliceWidth, sliceHeight, dataType);
                                                    break;
                                                }
                                                imageData.push(value);
                                                index++;
                                            }
                                        } catch (error) {
                                            console.error("error processing chunks: ", error);
                                        }
                                    },
                                });
                            } else {
                                throw new Error('Network response was not ok');
                            }
                        })*/
                }
            }
            /*for (let i = 0; i < images.length; i++) {
                let item = images[i];
                let img = item.image;
                img.onload = function(){
                    context.drawImage(img, item.col, item.row, item.col * sliceWidth, item.row * sliceHeight);
                    context.beginPath();
                    context.moveTo(30,96);
                    context.lineTo(70,66);
                    context.lineTo(103,76);
                    context.lineTo(170,15);
                    context.stroke();
                  };
                
            }*/
            element.replaceWith(canvas);
        }

    });

    function renderImage(img, element, canvas, ctx, col, row, sliceWidth, sliceHeight, dataType) {
        ctx.drawImage(
            img,
            col * sliceWidth,
            row * sliceHeight,
            sliceWidth,
            sliceHeight,
            col * sliceWidth,
            row * sliceHeight,
            sliceWidth,
            sliceWidth
        );
    }

    function replaceWith(el1, el2) {
        document.replaceWith(el1, el2);
    }

    function getImageType(filename) {
        const extensionMatch = /\.([a-zA-Z0-9]+)$/.exec(filename);
        console.log(extensionMatch)
        if (extensionMatch && extensionMatch[1]) {
            // Convert the extension to lowercase for comparison
            const extension = extensionMatch[1].toLowerCase();

            // List of known image extensions (you can expand this list as needed)
            const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg'];

            const imageTypes = [
                {
                    'ext': 'jpg',
                    'type': 'image/jpg'
                },
                {
                    'ext': 'jpeg',
                    'type': 'image/jpeg'
                },
                {
                    'ext': 'png',
                    'type': 'image/png'
                },
                {
                    'ext': 'gif',
                    'type': 'image/gif'
                },
                {
                    'ext': 'bmp',
                    'type': 'image/bmp'
                },
                {
                    'ext': 'svg',
                    'type': 'image/svg'
                }
            ];

            return imageTypes.find((it) => it.ext === extension);
        }
    }

});