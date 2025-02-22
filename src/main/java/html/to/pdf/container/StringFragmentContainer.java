package html.to.pdf.container;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StringFragmentContainer {
    private String textFragment;
    private Integer level = 0;
    private boolean listContext = false;
    private Object fragmentNode;
}
