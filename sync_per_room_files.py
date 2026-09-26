import json
import os

def sync():
    with open('src/main/resources/questions/questions.json', 'r', encoding='utf-8') as f:
        qs = json.load(f)

    room_map = {}
    for q in qs:
        r = q['room']
        room_map.setdefault(r, []).append(q)

    for r, r_qs in room_map.items():
        # Write room json
        json_path = f'questions/{r}.json'
        os.makedirs(os.path.dirname(json_path), exist_ok=True)
        items = []
        for q in r_qs:
            ans_idx = {'A': 0, 'B': 1, 'C': 2, 'D': 3}.get(q['correctAnswer'], 0)
            items.append({
                'text': q['text'],
                'options': [q['optionA'], q['optionB'], q['optionC'], q['optionD']],
                'answer': str(ans_idx)
            })
        with open(json_path, 'w', encoding='utf-8') as jf:
            json.dump({'questions': items}, jf, indent=2, ensure_ascii=False)
            
        # Write room txt
        txt_paths = [f'questions/{r}.txt', f'src/main/resources/questions/{r}.txt']
        for tp in txt_paths:
            os.makedirs(os.path.dirname(tp), exist_ok=True)
            with open(tp, 'w', encoding='utf-8') as tf:
                for q in r_qs:
                    ans_num = {'A': 1, 'B': 2, 'C': 3, 'D': 4}.get(q['correctAnswer'], 1)
                    tf.write("Q: " + q['text'] + "\n")
                    tf.write("A: " + q['optionA'] + "\n")
                    tf.write("B: " + q['optionB'] + "\n")
                    tf.write("C: " + q['optionC'] + "\n")
                    tf.write("D: " + q['optionD'] + "\n")
                    tf.write("Answer: " + str(ans_num) + "\n\n")

    print(f"Synced {len(room_map)} rooms to individual .json and .txt files successfully!")

if __name__ == '__main__':
    sync()
