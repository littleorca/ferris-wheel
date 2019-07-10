import React, { Component } from 'react';
import SpreadsheetClient from './SpreadsheetClient';
import {
  Workbook, WorkbookEditor, WorkbookPresenter
} from '@littleorca/ferris-wheel';
import Action from "@littleorca/ferris-wheel/lib/action/Action";
import WorkbookOperation from "@littleorca/ferris-wheel/lib/action/WorkbookOperation";
import '@littleorca/ferris-wheel/theme/default/theme.css';
import './App.css';

class App extends Component {

  static defaultProps = {
    wsUri: 'ws://' + window.location.host + '/wsapi'
  };

  constructor(props) {
    super(props);

    this.state = {
      presentation: false,
    };


    this.service = new SpreadsheetClient(this.props.wsUri);
    this.setEditMode = this.setEditMode.bind(this);
    this.setPresentationMode = this.setPresentationMode.bind(this);
  }

  componentDidMount() {
    this.openWorkbookAfterServiceReady();
  }

  openWorkbookAfterServiceReady() {
    if (this.service.isReady()) {
      this.openWorkbook();
    } else {
      setTimeout(() => this.openWorkbookAfterServiceReady(), 100);
    }
  }

  openWorkbook() {
    const action = new Action();
    action.openWorkbook = new WorkbookOperation();
    this.service.call({
      txId: 0,
      action: action,
    }, resp => {
      console.log('open workbook', resp);
      if (resp.statusCode === 0) {
        this.setState({
          workbook: resp.workbook,
        });
      }
    });
  }

  setEditMode() {
    this.setState({ presentation: false });
  }

  setPresentationMode() {
    this.setState({ presentation: true });
  }

  render() {
    return (
      <div className="wrapper">

        <div className="header">
          <ul>
            <li className={this.state.presentation ? "" : "selected"}>
              <a href="javascript:void(0);" onClick={this.setEditMode}>编辑模式</a>
            </li>
            <li className={this.state.presentation ? "selected" : ""}>
              <a href="javascript:void(0);" onClick={this.setPresentationMode}>展示模式</a>
            </li>
          </ul>
        </div>

        <div className="content">
          {this.renderContent()}
        </div>
      </div>
    );
  }

  renderContent() {
    const workbook = this.state.workbook;
    if (typeof workbook !== 'undefined') {
      return (
        this.state.presentation ?
          (
            <WorkbookPresenter
              workbook={workbook}
              service={this.service} />
          ) : (
            <WorkbookEditor
              workbook={workbook}
              service={this.service} />
          )
      );

    } else {
      return <div className="loading">Loading...</div>;
    }
  }

}

export default App;
