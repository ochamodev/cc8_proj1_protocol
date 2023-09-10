document.addEventListener("DOMContentLoaded", () => {
    const elementsWithHermesSrc = document.querySelectorAll('[hermes-src]');

    elementsWithHermesSrc.forEach(async element => {
        const resource = element.getAttribute('hermes-src');
        console.debug("requested resource: " + resource);
        let response = await fetch(resource + '?hermes=true');
        for await (const chunk of response.body) {
           console.log(chunk);
        }
    });

});