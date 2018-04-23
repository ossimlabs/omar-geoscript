<%--
  Created by IntelliJ IDEA.
  User: sbortman
  Date: 4/11/11
  Time: 12:56 PM
  To change this template use File | Settings | File Templates.
--%>

<table cellspacing="1" width="720" border="1">
  <g:each var="property" in="${properties}">
    <tr>
      <td colspan="2">
        <b>${property.value}</b>: &nbsp;${entry[property.key]?.encodeAsHTML()}
      </td>
    </tr>
  </g:each>
  <tr>
    <td colspan="2">
      <b>Minimum Latitude</b>: &nbsp;${bounds?.minY}&nbsp;${minLatDMS}
    </td>
  </tr>
  <tr>
    <td colspan="2">
      <b>Minimum Longitude</b>: &nbsp;${bounds?.minX}&nbsp;${minLonDMS}
    </td>
  </tr>
  <tr>
    <td colspan="2">
      <b>Maximum Latitude</b>: &nbsp;${bounds?.maxY}&nbsp;${maxLatDMS}
    </td>
  </tr>
  <tr>
    <td colspan="2">
      <b>Maximum Longitude</b>: &nbsp;${bounds?.maxX}&nbsp;${maxLonDMS}
    </td>
  </tr>
  <tr>
    <td colspan="2">
      <b>OMAR Link</b>: <a href="${tlvURL}">...</a>
    </td>
    <td colspan="2">
      <b>KML Link</b>: <a
        href="${superOverlayURL}">...</a>
    </td>
  </tr>
</table>
<br/>
