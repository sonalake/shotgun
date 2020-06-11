// Action show hotspots, and hide commit details
$('#show_hotspots').click(() => {
    $('#hotspots').show();
    $('#commit-details').hide();
});

// Action show a specific commit log, in the commit details
// date -> the commit's date
// value -> the shotgun score
// resultDate -> the commit history on this date
const show_commit_log = (date, value, resultData) => {
    // hide the hostspots, and clear anything that's already there
    $('#hotspots').hide();
    $('#commit-details').show();
    $('#commit-summary').empty();
    $('#commit-log').empty();

    // This date is the date we're looking for, we loop through all the commits
    // and only work with the commits done on this given day
    const check = (date.getTime() / 1000) - (date.getTimezoneOffset() * 60);
    for (const i in resultData.commitData) {
        const candidate = resultData.commitData[i];

        if (check === candidate.epochSecond) {
            // add the common summary for this commit day
            $('#commit-summary').append('<p>').append('<ul>')
                .append($(`<li>Date: ${date}</li>`))
                .append($(`<li>Median Score: ${candidate.score} <small class="text-muted">
              - (${candidate.commits.slice().reverse().map(i => i.score).join(",")})</small> </li>`))
                .append($(`<li>Commits: ${candidate.commits.length}</li>`));

            // now look through all the commits for today
            for (const c in candidate.commits) {
                const commit = candidate.commits[c];

                // we span these, so each file entry is in the same row
                let isFirst = true;
                for (f in commit.entries) {
                    const file = commit.entries[f];
                    const row = $('<tr>');
                    if (isFirst) {
                        row.append($(`<td rowspan="${commit.entries.length}">${commit.score}</td>`));
                        row.append($(`<td rowspan="${commit.entries.length}">${commit.committer}
                    <br/><sub>${commit.commit}</sub>
                    <br/><small class="text-muted">${commit.message}</small>
                    </td>`));
                        isFirst = false;
                    }

                    row.append($(`<td><small class="text-muted">${file.changeType}</small></td>`));
                    row.append($(`<td><small class="text-muted">${file.sourceSet}</small></td>`));
                    row.append($(`<td><small class="text-muted">${file.path}</small></td>`));

                    $('#commit-log').append(row)
                }
            }
            return;
        }
    }

    // if we get this far then there were no commits, say so
    $('#commit-summary').append('<p>').append('<ul>')
        .append($(`<li>Date: ${date}</li>`))
        .append($(`<li>No commits</li>`));

};

// given a set of result data, add the commit set data - these are those entries in the hotspot model where
// multiple files are updated in the same commit multiple times
const add_commit_sets = (resultData) => {
    for (const i in resultData.busySets) {
        const busySet = resultData.busySets[i]
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
// given a set of result data, add the busy file data - these are those entries in the hotspot model where
// files are updated multiple times
const add_busy_files = (resultData) => {

    for (const i in resultData.busyFiles) {
        const busyFile = resultData.busyFiles[i]
        const row = $(
            `<tr>
            <td><small>${busyFile.count}</small></td>
            <td><small>${busyFile.file}</small></td>
          </tr>`
        );

        $('#commit-files').append(row)
    }

}

// Build the heatmap calendar for the hotspot modl
const build_calendar = (resultData, heatData) => {

    const inner_show_commit_log = (date, value) => show_commit_log(date, value, resultData);
    add_commit_sets(resultData);
    add_busy_files(resultData);

    const dates = Object.keys(heatData);
    let startEpoch = parseInt(dates[0]);
    let endEpoch = parseInt(dates[dates.length - 1]);

    const startDate = new Date(parseInt(startEpoch) * 1000);
    const endDate = new Date(parseInt(endEpoch) * 1000);

    // limit the presentation to a year
    const YEAR = 12;
    const full_range = 1 + endDate.getMonth() - startDate.getMonth() +
        (YEAR * (endDate.getFullYear() - startDate.getFullYear()));
    const range = Math.min(YEAR, full_range);

    // if we have less than a year, we just render the data we have, starting
    // at the start date
    let renderStartDate = startDate;
    // if we have more data than 1 year then we add buttons to allow the user
    // to move back and forth
    if (range !== full_range) {
        // otherwise we render it by calendar year
        // with the *last* year first
        // starting in january
        renderStartDate = new Date(endDate.getFullYear(), 0, 1);
        $('#calendar-buttons')
            .append(`    <button type="button" class="btn btn-primary" id="previous">Previous</button>
                         <button type="button" class="btn btn-primary" id="reset">Reset</button>
                         <button type="button" class="btn btn-primary" id="next">Next</button>  `);

        let currentStart = renderStartDate;
        $('#next').on("click", () => {
            currentStart = new Date(currentStart.getFullYear() + 1, currentStart.getMonth(), currentStart.getDate());
            cal.jumpTo(currentStart, true);
        })

        $('#reset').on("click", () => {
            currentStart = renderStartDate;
            cal.jumpTo(currentStart, true);
        })


        $('#previous').on("click", () => {
            currentStart = new Date(currentStart.getFullYear() - 1, currentStart.getMonth(), currentStart.getDate());
            cal.jumpTo(currentStart, true);
        })
    }

    // render the calendar
    var cal = new CalHeatMap();
    cal.init({
        start: renderStartDate,
        range: range,
        domain: "month",
        subDomain: "x_day",
        label: {
            position: "top"
        },
        legend: LEGEND,
        itemName: ["score", "score"],
        legend: LEGEND,
        legendCellSize: 20,
        domainLabelFormat: "%B %Y",
        subDomainTitleFormat: {
            empty: "{date}",
            filled: "{date}: {name} -> {count}  "
        },
        cellSize: 15,
        domainGutter: 15,
        onClick: inner_show_commit_log,
        data: heatData
    });
}



build_calendar(resultData, HEATMAP);
