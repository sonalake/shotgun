<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Project description</title>
  <script>
  <!--
  <#include "jquery-3.4.1.min.js" parse=false>
   -->

  </script>
  <script>
  <!--
  <#include "d3.v3.min.js" parse=false>
  -->
  </script>
  <script>
  <!--
  <#include "cal-heatmap.3.3.10.min.js" parse=false>
  -->
  </script>
  <style> <#include "cal-heatmap.css" parse=false></style>
  <style> <#include "bootstrap.min.4.3.1.css" parse=false></style>

</head>
<body>

<div class="container-fluid">

  <h1>Shotgun Complexity</h1>
  <h2>${repo}</h2>
  <small class="text-muted">Calculated by <a href="https://bitbucket.org/sonalake/shotgun/src/master/README.md">shotgun</a></small>

  <div id="cal-heatmap"></div>


  <small class="text-muted">
    How complex are commits over time, where the darker a day is, the more complex the commits
    are. Click on a given day for more details.
  </small>
  <br/>
  <div id ="calendar-buttons">
  </div>



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
        <table class="table table-sm">
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
        <table class="table table-sm">
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
    <table class="table table-sm">
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
-->
</script>

<script type="text/javascript">
<!--
<#include "report.js" parse=false>
-->
</script>
</body>
</html>
