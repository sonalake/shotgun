<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Project description</title>
  <script type="text/javascript" src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
  <script type="text/javascript" src="https://d3js.org/d3.v3.min.js"></script>
  <script type="text/javascript" src="https://cdn.jsdelivr.net/cal-heatmap/3.3.10/cal-heatmap.min.js"></script>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/cal-heatmap/3.3.10/cal-heatmap.css"/>
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
        integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">


</head>
<body>

<div class="container-fluid">

  <h1>Shotgun Complexity</h1>
  <h2>${repo}</h2>

  <div id="cal-heatmap"></div>
  <small class="text-muted">
    How complex are commits over time, where the darker a day is, the more complex the commits
    are. Click on a given day for more details.
  </small>
  <hr/>


  <button type="button" class="btn btn-dark" id="show_hotspots">HotSpots</button>
  <small class="text-muted">What are the most committed elements </small>


  <hr/>
  <div id="hotspots">
    <div class="row">
      <div class="col-6">
        <div class="alert alert-success" role="alert">
          Active Commit Sets
        </div>
        <small class="text-muted">These are the sets of files committed most often, ignoring sets that are too
          small</small>
        <table class="table">
          <thead>
          <tr>
            <th scope="col">Count</th>
            <th scope="col">Set</th>
          </tr>
          </thead>
          <tbody id="commit-sets">
          </tbody>
        </table>
      </div>
      <div class="col-6">
        <div class="alert alert-success" role="alert">
          Active Files
        </div>
        <small class="text-muted">These are the files committed most often</small>
        <table class="table">
          <thead>
          <tr>
            <th scope="col">Count</th>
            <th scope="col">File</th>
          </tr>
          </thead>
          <tbody id="commit-files">
          </tbody>
        </table>
      </div>
    </div>


  </div>

  <div id="commit-details" style="display:none">
    <div id="commit-summary"></div>
    <table class="table">
      <thead>
      <tr>
        <th scope="col">Score</th>
        <th scope="col">Commit</th>
        <th scope="col">Files</th>
      </tr>
      </thead>
      <tbody id="commit-log">
      </tbody>
    </table>
  </div>

</div>


<script type="text/javascript">
<!--
    const LEGEND = [${legend}];
    const HEATMAP = ${heatMap};
    const BUSY_SETS = ${busySets};
    const BUSY_FILES = ${busyFiles};
    const COMMIT_DATA = ${commitData};
    const resultData = {
      "busyFiles" : BUSY_FILES,
      "busySets" : BUSY_SETS,
      "commitData" : COMMIT_DATA
    }


<#include "report.js" parse=false>

-->


</script>
</body>
</html>
