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
  <style> <#include "custom.css" parse=false></style>

</head>
<body>
  <div class="container">
    <h1 class="title">Shotgun Complexity</h1>
    <h2 class="sub-title">${repo}</h2>
    <div class="container-fluid">
      <div class="heatmap-container">
        <div class="heatmap-controller">
          <div
            id="previousButtonContainer"
            class="btn-navigate-container"
          ><button type="button" class="btn btn-calendar" id="previous">&#8592;</button>
        </div>
          <div class="heatmap" id="cal-heatmap"></div>
          <div id="nextButtonContainer" class="btn-navigate-container">
            <button type="button" class="btn btn-calendar" id="next">&#8594;</button>
          </div>
        </div>
        <small class="text-muted text-legend small-block">
          How complex are commits over time, where the darker a day is, the
          more complex the commits are. Click on a given day for more details.
        </small>
      </div>

      <br />

      <div class="btn-container">
        <button type="button" class="btn btn-dark" id="reset">Reset</button>
        <button type="button" class="btn btn-dark" id="show_hotspots">
          HotSpots
        </button>
        <small class="text-muted"
          >What are the most committed elements
        </small>
      </div>

      <div id="hotspots">
        <div class="row">
          <div class="col-12">
            <div class="hotspots-container">
              <div class="alert alert-success" role="alert">
                Active Commit Sets
              </div>
              <small class="text-muted"
                >These are the sets of files committed most often, ignoring sets
                that are too small</small
              >
              <table class="table table-sm table-without-margin">
                <thead>
                  <tr>
                    <th scope="col">Count</th>
                    <th scope="col">File</th>
                  </tr>
                </thead>
              </table>
              <div class="active-commits-container">
                <table class="table table-sm">
                  <tbody id="commit-sets"></tbody>
                </table>
              </div>
            </div>
          </div>
          <div class="col-12">
            <div class="hotspots-container">
              <div class="alert alert-success" role="alert">Active Files</div>
              <small class="text-muted"
                >These are the files committed most often</small
              >
              <table class="table table-sm table-without-margin">
                <thead>
                  <tr>
                    <th scope="col">Count</th>
                    <th scope="col">File</th>
                  </tr>
                </thead>
              </table>
              <div class="active-files-container">
                <table class="table table-sm">
                  <tbody id="commit-files"></tbody>
                </table>
              </div>
            </div>
          </div> 
        </div>
      </div>

      <div id="commit-details" style="display: none">
        <h4>Commit details</h4>
        <div id="commit-summary"></div>
        <div>
          <ul class="list-group" id="commit-list"></ul>
        </div>
      </div>
      <div class="footer">
        <small class="text-muted small-block">
          Calculated by
          <a href="https://github.com/sonalake/shotgun/blob/main/README.md">
            shotgun
          </a>
        </small>
      </div>
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
      "commitData" : COMMIT_DATA,
      "firstCommitDate": new Date(
        Math.min(...COMMIT_DATA.map((e) => new Date(e.date)))
      ),
      "lastCommitDate": new Date(
        Math.max(...COMMIT_DATA.map((e) => new Date(e.date)))
      )
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
