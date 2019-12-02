
     $('#show_hotspots').click(() =>{
      $('#hotspots').show();
      $('#commit-details').hide();
     });

    const show_commit_log = (date, value, resultData) => {
      $('#hotspots').hide();
      $('#commit-details').show();
      $('#commit-summary').empty();
      $('#commit-log').empty();

      const check = (date.getTime() / 1000) -  (date.getTimezoneOffset() * 60);
      for (const i in resultData.commitData) {
        const candidate = resultData.commitData[i];

        if (check === candidate.epochSecond) {

          $('#commit-summary').append('<p>').append('<ul>')
          .append($(`<li>Date: ${date}</li>`))
          .append($(`<li>Median Score: ${candidate.score} <small class="text-muted">
            - (${candidate.commits.slice().reverse().map(i => i.score).join(",")})</small> </li>`))
          .append($(`<li>Commits: ${candidate.commits.length}</li>`));

          for (const c in candidate.commits) {
            const commit = candidate.commits[c];
            const row = $(
            `<tr>
              <td>${commit.score}</td>
              <td>${commit.committer}
              <br/><sub>${commit.commit}</sub>
              <br/><small class="text-muted">${commit.message}</small>
              </td></tr>
            `);

            const commitFileTable = $(`
                <table class="table table-sm">
                  <thead>
                  <tr>
                    <th scope="col">Type</th>
                    <th scope="col">Source Set</th>
                    <th scope="col">File</th>
                  </tr>
                  </thead>
                </table>`);


            const files = $(`<tbody></tbody>`);

            for (f in commit.entries) {
              const file = commit.entries[f];
              files.append(`<tr>
              <td><small class="text-muted">${file.changeType}</small></td>
              <td><small class="text-muted">${file.sourceSet}</small></td>
              <td><small class="text-muted">${file.path}</small></td>
              </tr>`);
            }
            commitFileTable.append(files)
            row.append($('<td>').append(commitFileTable));
            $('#commit-log').append(row)
          }
          return;
        }
      }


      $('#commit-summary').append('<p>').append('<ul>')
          .append($(`<li>Date: ${date}</li>`))
          .append($(`<li>No commits</li>`));

    };

    const add_commit_sets = (resultData)=> {

      for (const i in resultData.busySets) {
      const busySet  = resultData.busySets[i]
        const row = $(`<tr><td><small>${busySet.count}</small></td></tr>`);
        const files = $(`<td></td>`);

        for (f in busySet.files) {
          const file = busySet.files[f];
          files.append(`<li><small class="text-muted">${file}</small></li>`);
        }
        row.append(files)
        $('#commit-sets').append(row)
      }

    }

    const add_busy_files = (resultData)=> {

      for (const i in resultData.busyFiles) {
      const busyFile  = resultData.busyFiles[i]
        const row = $(
        `<tr>
          <td><small>${busyFile.count}</small></td>
          <td><small>${busyFile.file}</small></td>
        </tr>`
        );

        $('#commit-files').append(row)
      }

    }

    const build_calendar = (resultData, heatData) => {

    const inner_show_commit_log  = (date, value) => show_commit_log(date, value, resultData);
    add_commit_sets(resultData);
    add_busy_files(resultData);

      const dates = Object.keys(heatData);
      let startEpoch = parseInt(dates[0]);
      let endEpoch = parseInt(dates[dates.length - 1]);

      const startDate = new Date (parseInt(startEpoch) * 1000);
      const endDate = new Date (parseInt(endEpoch) * 1000);

      const range = 1 + endDate.getMonth() - startDate.getMonth() +
        (12 * (endDate.getFullYear() - startDate.getFullYear()));

      var cal = new CalHeatMap();
      cal.init({
          start: startDate,
          range: range,
          domain: "month",
          subDomain: "x_day",
          label: {
            position: "top"
          },
          legend: LEGEND,
          itemName: ["score", "score"],
          legend: LEGEND,
          legendCellSize:20,
          subDomainTitleFormat: {
            empty: "{date}",
            filled: "{date}: {name} -> {count}  "
          },
          cellSize: 15,
          domainGutter: 15,
          onClick : inner_show_commit_log,
          data: heatData
        });
    }



    build_calendar(resultData, HEATMAP);
