const scroller = document.querySelector('.cards');

scroller.addEventListener('wheel', (e) => {
    if (e.deltaY !== 0) {
        e.preventDefault();
        scroller.scrollLeft += e.deltaY;
    }
}, { passive: false });
