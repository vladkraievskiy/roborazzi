package io.github.takahirom.roborazzi

import com.github.takahirom.roborazzi.CaptureResult
import com.github.takahirom.roborazzi.CompareReportResult
import com.github.takahirom.roborazzi.CompareSummary
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class CompareReportResultTest {

  @Test
  fun testJsonSerialization() {
    val summary = CompareSummary(10, 2, 3, 5)
    val captureResutlsResults = listOf(
      CaptureResult.Added(
        compareFile = File("/compare_file"),
        actualFile = File("/actual_file"),
        goldenFile = File("/golden_file"),
        timestampNs = 123456789,
      ),
      CaptureResult.Changed(
        compareFile = File("/compare_file"),
        goldenFile = File("/golden_file"),
        actualFile = File("/actual_file"),
        timestampNs = 123456789,
      ),
      CaptureResult.Unchanged(
        goldenFile = File("/golden_file"),
        timestampNs = 123456789
      )
    )

    val compareReportResult = CompareReportResult(summary, captureResutlsResults)

    val json = compareReportResult.toJson()
    val jsonSummary = json.getJSONObject("summary")
    val jsonResults = json.getJSONArray("results")

    // Test summary
    assertEquals(summary.total, jsonSummary.getInt("total"))
    assertEquals(summary.added, jsonSummary.getInt("added"))
    assertEquals(summary.changed, jsonSummary.getInt("changed"))
    assertEquals(summary.unchanged, jsonSummary.getInt("unchanged"))

    // Test capture results
    assertEquals(captureResutlsResults.size, jsonResults.length())

    for (i in 0 until jsonResults.length()) {
      val jsonResult = jsonResults.getJSONObject(i)
      val captureResult = captureResutlsResults[i]

      assertEquals(
        captureResult.compareFile?.absolutePath, jsonResult.optString("compare_file_path", null)
      )
      assertEquals(
        captureResult.goldenFile?.absolutePath,
        jsonResult.optString("golden_file_path", null)
      )
      assertEquals(
        captureResult.actualFile?.absolutePath,
        jsonResult.optString("actual_file_path", null)
      )
      assertEquals(captureResult.timestampNs, jsonResult.getLong("timestamp"))
    }
  }

  @Test
  fun testJsonDeserialization() {
    val jsonString = """
        {
            "summary": {
                "total": 10,
                "added": 2,
                "changed": 3,
                "unchanged": 5
            },
            "results": [
                {
                    "type": "added",
                    "compare_file_path": "compare_file",
                    "actual_file_path": "actual_file",
                    "timestamp": 123456789,
                },
                {
                    "type": "changed",
                    "compare_file_path": "compare_file",
                    "actual_file_path": "actual_file",
                    "golden_file_path": "golden_file",
                    "timestamp": 123456789,
                },
                {
                    "type": "unchanged",
                    "golden_file_path": "golden_file",
                    "timestamp": 123456789,
                }
            ]
        }
        """.trimIndent()

    val compareReportResult = CompareReportResult.fromJson(JSONObject(jsonString))
    val summary = compareReportResult.summary
    val compareReportCaptureResults = compareReportResult.compareReportCaptureResults

    // Test summary
    assertEquals(10, summary.total)
    assertEquals(2, summary.added)
    assertEquals(3, summary.changed)
    assertEquals(5, summary.unchanged)

    // Test capture results
    assertEquals(3, compareReportCaptureResults.size)

    val addedResult = compareReportCaptureResults[0] as CaptureResult.Added
    assertEquals(File("compare_file"), addedResult.compareFile)
    assertEquals(File("actual_file"), addedResult.actualFile)
    assertEquals(123456789, addedResult.timestampNs)

    val changedResult = compareReportCaptureResults[1] as CaptureResult.Changed
    assertEquals(File("compare_file"), changedResult.compareFile)
    assertEquals(File("actual_file"), changedResult.actualFile)
    assertEquals(File("golden_file"), changedResult.goldenFile)
    assertEquals(123456789, changedResult.timestampNs)

    val unchangedResult = compareReportCaptureResults[2] as CaptureResult.Unchanged
    assertEquals(File("golden_file"), unchangedResult.goldenFile)
    assertEquals(123456789, unchangedResult.timestampNs)
  }
}