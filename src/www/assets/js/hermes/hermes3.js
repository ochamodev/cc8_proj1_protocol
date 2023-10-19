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
        console.log("divWidth = " + element.clientWidth)
        console.log("divHeight = " + element.clientHeight)
        let elementWidth = element.clientWidth;
        let elementHeight = element.clientHeight;

        console.log("elementWidth = ", elementWidth);
        console.log("elementHeight = ", elementHeight);
        
        let response = await fetch(resource + '?hermes=true&hermesStep=1')
        if (response.ok) {
            let headers = response.headers;
            let rowCount = headers.get(rowCountHeader);
            let colCount = headers.get(colCountHeader);
            let imgWidth = headers.get(imgWidthHeader);
            let imgHeight = headers.get(imgHeightHeader);
            let chunkSrc = headers.get(chunkLocationHeader);
            let chunkName = headers.get(chunkNameHeader);
            const sliceWidth = imgWidth / colCount;
            const sliceHeight = imgHeight / rowCount;
            
            console.log("sliceWidth = ", sliceWidth, "sliceHeight = ", sliceHeight)
            console.log("sliceWidth = ", elementWidth / colCount, "sliceHeight = ", sliceHeight)
            for (let row = 0; row < rowCount; row++) {
                for (let col = 0; col < colCount; col++) {
                    let imagePath = `${chunkSrc}${chunkName}${row}_${col}.${dataType.ext}`;
                    let img = new Image(elementWidth / colCount);
                    img.src = imagePath;
                    
                    element.appendChild(img);
                }
            }
        }

    });

    function getImageType(filename) {
        const extensionMatch = /\.([a-zA-Z0-9]+)$/.exec(filename);
        console.log(extensionMatch)
        if (extensionMatch && extensionMatch[1]) {
            // Convert the extension to lowercase for comparison
            const extension = extensionMatch[1].toLowerCase();

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