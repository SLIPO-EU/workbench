import * as React from 'react';


class Zoom extends React.Component {

  constructor(props) {
    super(props);

    const htmlElement = document.getElementsByTagName('html')[0];
    const fontSize = htmlElement.style.fontSize;

    this.state = {
      fontSize: this.getFontSize(fontSize),
    };
  }

  getFontSize(fontSize) {
    return fontSize ? !Number.isInteger(fontSize) ? parseInt(fontSize.substring(0, fontSize.length - 2)) : fontSize : 16;
  }

  setFontSize(size) {
    const htmlElement = document.getElementsByTagName('html')[0];
    htmlElement.style.fontSize = `${size}px`;
  }

  zoomIn(e) {
    e.preventDefault();

    const { fontSize: current } = this.state;
    const fontSize = current + 2;

    this.setState({
      fontSize,
    });
    this.setFontSize(fontSize);
  }

  zoomOut(e) {
    e.preventDefault();

    const { fontSize: current } = this.state;
    const fontSize = current - 2;

    if (fontSize < 8) {
      return;
    }

    this.setState({
      fontSize,
    });
    this.setFontSize(fontSize);
  }

  render() {
    return (
      <div>
        <i className="fa fa-search-plus mr-2 btn p-0 text-muted" onClick={(e) => this.zoomIn(e)} />
        <i className="fa fa-search-minus mr-2 btn p-0 text-muted" onClick={(e) => this.zoomOut(e)} />
      </div>
    );
  }

}

export default Zoom;
