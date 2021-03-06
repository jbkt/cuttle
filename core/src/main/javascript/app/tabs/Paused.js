// @flow

import React from "react";
import { connect } from "react-redux";
import injectSheet from "react-jss";

import PopoverMenu from "../components/PopoverMenu";
import type { Workflow } from "../../datamodel";
import ExecutionLogs from "../components/ExecutionLogs";

type Props = {
  classes: any,
  workflow: Workflow
};

const Paused = ({ classes, workflow }: Props) => {
  let unpauseAll = () => fetch("/api/jobs/all/unpause", {method: "POST"});
  return (
    <div className={classes.container}>
      <h1 className={classes.title}>Paused executions</h1>
      <PopoverMenu
        className={classes.menu}
        items={[<span onClick={unpauseAll}>Unpause everything</span>]}
      />
      <ExecutionLogs
        workflow={workflow}
        columns={["job", "context", "status", "detail"]}
        request={(page, rowsPerPage, sort) =>
          `/api/executions/paused?stream=true&offset=${page * rowsPerPage}&limit=${rowsPerPage}&sort=${sort.column}&order=${sort.order}`}
        label="paused"
        defaultSort={{ column: "context", order: "asc" }}
      />
    </div>
  );
};

const styles = {
  container: {
    padding: "1em",
    flex: "1",
    display: "flex",
    flexDirection: "column",
    position: "relative"
  },
  title: {
    fontSize: "1.2em",
    margin: "0 0 16px 0",
    color: "#607e96",
    fontWeight: "normal"
  },
  menu: {
    position: "absolute",
    top: "1em",
    right: "1em"
  }
};

const mapStateToProps = ({ workflow }) => ({ workflow });

export default connect(mapStateToProps)(
  injectSheet(styles)(Paused)
);
