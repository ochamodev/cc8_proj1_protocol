document.addEventListener("DOMContentLoaded", () => {
    const elementsWithHermesSrc = document.querySelectorAll('[hermes-src]');

    elementsWithHermesSrc.forEach(async element => {
        const resource = element.getAttribute('hermes-src');
        const dataType = getImageType(resource);
        console.log("heigth: " + element.clientHeight);
        console.log("width: " + element.clientWidth);
        //console.debug("requested resource: " + resource);
        //console.log("resource type: " + JSON.stringify(dataType));
        //const response = await fetch(resource + '?hermes=true&hermesStep=1');
        if (true) {
            //console.log(response.headers);
            fetch(resource + '?hermes=true&hermesStep=2')
                .then((response) => {
                    console.log(response);
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
                                        element.onload="this.style.display = 'block'"
                                        element.src = imageUrl;
                                        console.log(resource);
                                        console.log("done");
                                        controller.close();
                                        break;
                                    }
                                    //console.log("value = ", value);
                                    imageData.push(value);
                                    index++;
                                }
                            } catch (error) {
                                console.error("error processing chunks: ", error);
                            }
                        },
                    });
                })
                .catch(error => {
                    console.error("Error: ", error);
                });
        }

    });


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